import 'package:flutter/material.dart';

import '../app_scope.dart';
import '../models/user.dart';
import 'analyze_screen.dart';
import 'appointments_screen.dart';
import 'history_screen.dart';
import 'notifications_screen.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  int _tab = 0;

  static const _titles = ['Analyze an X-ray', 'History', 'Appointments', 'Notifications'];

  @override
  Widget build(BuildContext context) {
    final state = AppScope.of(context);
    final user = state.user!;
    return Scaffold(
      appBar: AppBar(
        title: Text(_titles[_tab]),
        actions: [
          PopupMenuButton<String>(
            icon: CircleAvatar(child: Text(user.name.isEmpty ? '?' : user.name[0].toUpperCase())),
            itemBuilder: (context) => [
              PopupMenuItem<String>(
                enabled: false,
                child: ListTile(
                  contentPadding: EdgeInsets.zero,
                  title: Text(user.name),
                  subtitle: Text('${roleLabel(user.role)} · ${user.email}'
                      '${user.isDoctor && !user.verified ? '\nNot verified yet' : ''}'),
                ),
              ),
              const PopupMenuDivider(),
              const PopupMenuItem<String>(value: 'logout', child: Text('Log out')),
            ],
            onSelected: (value) {
              if (value == 'logout') state.logout();
            },
          ),
          const SizedBox(width: 8),
        ],
      ),
      body: IndexedStack(
        index: _tab,
        children: [
          AnalyzeScreen(onAnalyzed: () => setState(() {})),
          HistoryScreen(visible: _tab == 1),
          AppointmentsScreen(visible: _tab == 2),
          NotificationsScreen(visible: _tab == 3),
        ],
      ),
      bottomNavigationBar: NavigationBar(
        selectedIndex: _tab,
        onDestinationSelected: (index) => setState(() => _tab = index),
        destinations: const [
          NavigationDestination(icon: Icon(Icons.document_scanner_outlined), selectedIcon: Icon(Icons.document_scanner), label: 'Analyze'),
          NavigationDestination(icon: Icon(Icons.history), label: 'History'),
          NavigationDestination(icon: Icon(Icons.event_outlined), selectedIcon: Icon(Icons.event), label: 'Appointments'),
          NavigationDestination(icon: Icon(Icons.notifications_outlined), selectedIcon: Icon(Icons.notifications), label: 'Notifications'),
        ],
      ),
    );
  }
}
