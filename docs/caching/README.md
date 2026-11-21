# Caching component with AOP (Lab 6, Problem 2)

Team: Federica, Teodora.

## Work split

| | Federica: cache core and aspect | Teodora: `@Cached`, API, model comparison |
|---|---|---|
| Code | `cache.CacheStore`, `cache.CacheStats`, `cache.CacheRegistry` (TTL, LRU removal, statistics, thread safety) | `cache.Cached` and the `annotatedMethod` pointcut / `cacheAnnotated` advice |
| | `cache.CacheKeys` (SHA-256 of the image content) | `api.CacheController` (`GET /api/cache/stats`, `DELETE /api/cache`) |
| | `aspect.CachingAspect`: the `modelDetection` and `configurationLoad` pointcuts and their advice | `@Cached` on `FractureAnalysisFacade.compareModels(XRayImage)` |
| | `config.ConfigLoader.load` (reads the configuration file) | |
| Tests | `CacheStoreTest`, `CachingAspectTest`, `ConfigurationCacheTest` | `CachedAnnotationTest`, `CacheControllerTest`, `ModelComparisonCacheTest` |
| Presentation | pointcuts and weaving, image hash keys, TTL and LRU | `@Cached`, statistics endpoint, live hit/miss demo |
| Documentation | | this README |

An AspectJ aspect that caches expensive results, so that:
- the same X-ray never goes through the same detection model twice (key: model + version + hash of the image content);
- comparing models (a doctor runs every model on one X-ray) is cached as a whole for 10 minutes, and each model's result is cached as well;
- the configuration file is not read again for 5 minutes;
- any method can be cached with one annotation, `@Cached`.

Every cache has an expiration time (TTL), a maximum size (the least recently used entry is removed)
and statistics (hits, misses, hit rate, evictions, expirations), available at `GET /api/cache/stats`.

None of the cached classes (`YoloModel`, `RfDetrModel`, `ConfigLoader`, `FractureAnalysisFacade`) contains caching code
(the facade only carries the `@Cached` annotation): the aspect is woven into them at compile time.

## How it works

```
caller ──► YoloModel.detect(image)
              │  (woven by AspectJ)
              ▼
        CachingAspect  @Around
              │ key = "YOLO:v8:" + SHA-256(image file content)
              ├── in the cache and not expired? ──► return the cached result   (hit)
              └── otherwise ──► run the real detect() ──► save the result ──► return   (miss)
```

| Class | Role |
|---|---|
| `aspect.CachingAspect` | the aspect: three pointcuts, one `@Around` advice for each |
| `cache.CacheStore` | one named cache: TTL, maximum size with LRU removal, statistics, thread safe |
| `cache.CacheRegistry` | all the caches by name (`analysis`, `configuration`, and the ones created by `@Cached`, e.g. `model-comparison`) |
| `cache.CacheKeys` | keys: an `XRayImage` is identified by the SHA-256 hash of its file content |
| `cache.Cached` | annotation to cache any method: `@Cached(name = "...", ttlSeconds = ..., maxEntries = ...)` |
| `cache.CacheStats` | statistics of one cache; `hitRate = hits / (hits + misses)` |
| `api.CacheController` | `GET /api/cache/stats`, `DELETE /api/cache` (need a JWT) |

## Pointcuts

| Pointcut | Cache | Key | TTL / size |
|---|---|---|---|
| `execution(AnalysisResult DetectionModel+.detect(XRayImage))` | `analysis` | model name + version + image content hash | 24 h / 500 |
| `execution(Properties ConfigLoader+.load(String))` | `configuration` | file path | 5 min / 20 |
| `execution(@Cached * *(..))` | from the annotation | method + arguments (images by content hash) | from the annotation |
| ↳ used on `FractureAnalysisFacade.compareModels(XRayImage)` | `model-comparison` | X-ray content hash | 10 min / 100 |

`DetectionModel+` means every subclass, so `YoloModel`, `RfDetrModel` and any model added later are cached
automatically. The build log lists every place the aspect was woven
("Join point ... advised by around advice from CachingAspect").

## Rules

- A result is cached only if the method returned normally and the result is not null; exceptions are never cached.
- The image key uses the file content, not the file name: the same X-ray uploaded twice under different names is a hit;
  a different X-ray under the same name is a miss.
- If the image file cannot be read, there is no reliable key, so the call is not cached.
- Each model has its own entry for the same image, so model comparison gets one result per model.
- The cached comparison is an unmodifiable list, so one caller cannot change it for the others.

## Statistics

`GET /api/cache/stats` (with `Authorization: Bearer <token>`):

```json
[
  { "name": "analysis", "size": 1, "maxEntries": 500, "ttlSeconds": 86400,
    "hits": 1, "misses": 1, "hitRate": 0.5, "evictions": 0, "expirations": 0 },
  { "name": "configuration", "size": 0, "maxEntries": 20, "ttlSeconds": 300,
    "hits": 0, "misses": 0, "hitRate": 0.0, "evictions": 0, "expirations": 0 }
]
```

`DELETE /api/cache` empties all caches and resets the statistics (`204 No Content`).

## Using @Cached

The model comparison in `FractureAnalysisFacade`:

```java
@Cached(name = "model-comparison", ttlSeconds = 600, maxEntries = 100)
public List<AnalysisResult> compareModels(XRayImage image) { ... }
```

`compareModels(doctor, source, bodyPart)` converts the source to an `XRayImage` and calls this method.
Any other method can be cached the same way; the cache is created on the first call and appears in the statistics.

## Tests (20)

| Test class | What it checks |
|---|---|
| `CacheStoreTest` (5) | miss then hit; expiration after the TTL (with a test clock); LRU removal when full; null not cached; clear resets the statistics |
| `CachingAspectTest` (5) | same image analyzed once per model; same content under another file name is a hit; different image is a miss; one result per model (model comparison); unreadable image is not cached |
| `ConfigurationCacheTest` (2) | the configuration file is read once, then served from the cache; a missing file is not cached |
| `CachedAnnotationTest` (2) | `@Cached` on any method; exceptions are not cached |
| `CacheControllerTest` (3) | statistics through the API; clear; a token is required |
| `ModelComparisonCacheTest` (2) | the same X-ray (even under another file name) is compared once; a different X-ray is compared again |

The existing `FractureDetectorTest` (Ianis) still passes: its images are not real files, so they are not cached.

## Current limits

- The image adapters (`ImageFileAdapter`, ...) are still skeletons, so `compareModels(doctor, source, bodyPart)` works
  end to end only after they are implemented; `compareModels(image)` and its cache already work.
- The caches are in memory, per backend instance: they are emptied when the backend restarts.
- A cached `AnalysisResult` is the same object for every caller with the same image and model.
