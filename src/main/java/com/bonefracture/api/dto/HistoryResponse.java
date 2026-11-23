package com.bonefracture.api.dto;

import com.bonefracture.xray.XRayImage;

import java.util.List;

public record HistoryResponse(List<ItemDto> items) {

    public static HistoryResponse from(List<XRayImage> images) {
        List<ItemDto> items = images.stream()
                .map(ItemDto::from)
                .toList();
        return new HistoryResponse(items);
    }

    public record ItemDto(String path, String uploadDate, int resultCount) {
        public static ItemDto from(XRayImage image) {
            return new ItemDto(
                    image.getPath(),
                    String.valueOf(image.getUploadDate()),
                    image.getResults().size());
        }
    }
}
