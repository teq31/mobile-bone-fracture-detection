import 'package:flutter/material.dart';

import 'app_scope.dart';
import 'screens/admin_screen.dart';
import 'screens/home_screen.dart';
import 'screens/login_screen.dart';

void main() {
  runApp(BoneFractureApp(state: AppState()));
}

class BoneFractureApp extends StatelessWidget {
  final AppState state;

  const BoneFractureApp({super.key, required this.state});

  @override
  Widget build(BuildContext context) {
    return AppScope(
      state: state,
      child: MaterialApp(
        title: 'Bone Fracture Detection',
        debugShowCheckedModeBanner: false,
        theme: ThemeData(
          colorSchemeSeed: const Color(0xFF2563EB),
          useMaterial3: true,
          inputDecorationTheme: const InputDecorationTheme(border: OutlineInputBorder()),
        ),
        darkTheme: ThemeData(
          colorSchemeSeed: const Color(0xFF2563EB),
          brightness: Brightness.dark,
          useMaterial3: true,
          inputDecorationTheme: const InputDecorationTheme(border: OutlineInputBorder()),
        ),
        home: const StartScreen(),
      ),
    );
  }
}

class StartScreen extends StatefulWidget {
  const StartScreen({super.key});

  @override
  State<StartScreen> createState() => _StartScreenState();
}

class _StartScreenState extends State<StartScreen> {
  bool _restoring = true;
  bool _started = false;

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    if (!_started) {
      _started = true;
      _restore();
    }
  }

  Future<void> _restore() async {
    final state = AppScope.of(context);
    final user = await state.auth.restoreSession();
    if (!mounted) return;
    state.user = user;
    setState(() => _restoring = false);
  }

  @override
  Widget build(BuildContext context) {
    if (_restoring) {
      return const Scaffold(body: Center(child: CircularProgressIndicator()));
    }
    final user = AppScope.of(context).user;
    if (user == null) return const LoginScreen();
    return user.isAdmin ? const AdminHomeScreen() : const HomeScreen();
  }
}
