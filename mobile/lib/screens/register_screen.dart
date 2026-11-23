import 'package:flutter/material.dart';

import '../app_scope.dart';
import '../models/user.dart';
import '../widgets/common.dart';

class RegisterScreen extends StatefulWidget {
  const RegisterScreen({super.key});

  @override
  State<RegisterScreen> createState() => _RegisterScreenState();
}

class _RegisterScreenState extends State<RegisterScreen> {
  final _form = GlobalKey<FormState>();
  final _name = TextEditingController();
  final _email = TextEditingController();
  final _password = TextEditingController();
  Role _role = Role.patient;
  bool _busy = false;

  @override
  void dispose() {
    _name.dispose();
    _email.dispose();
    _password.dispose();
    super.dispose();
  }

  Future<void> _register() async {
    if (!_form.currentState!.validate()) return;
    setState(() => _busy = true);
    final state = AppScope.of(context);
    try {
      final user = await state.auth.register(_name.text, _email.text, _password.text, _role);
      if (!mounted) return;
      Navigator.of(context).popUntil((route) => route.isFirst);
      state.user = user;
    } catch (e) {
      if (mounted) showError(context, e);
    } finally {
      if (mounted) setState(() => _busy = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Create an account')),
      body: SafeArea(
        child: Center(
          child: SingleChildScrollView(
            padding: const EdgeInsets.all(24),
            child: ConstrainedBox(
              constraints: const BoxConstraints(maxWidth: 420),
              child: Form(
                key: _form,
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    TextFormField(
                      controller: _name,
                      textCapitalization: TextCapitalization.words,
                      decoration: const InputDecoration(labelText: 'Full name'),
                      validator: (v) => v != null && v.trim().isNotEmpty ? null : 'Enter your name',
                    ),
                    const SizedBox(height: 12),
                    TextFormField(
                      controller: _email,
                      keyboardType: TextInputType.emailAddress,
                      decoration: const InputDecoration(labelText: 'Email'),
                      validator: (v) => v != null && v.contains('@') ? null : 'Enter a valid email',
                    ),
                    const SizedBox(height: 12),
                    TextFormField(
                      controller: _password,
                      obscureText: true,
                      decoration: const InputDecoration(labelText: 'Password', helperText: 'At least 8 characters'),
                      validator: (v) => v != null && v.length >= 8 ? null : 'At least 8 characters',
                    ),
                    const SizedBox(height: 20),
                    Text('I am a', style: Theme.of(context).textTheme.titleSmall),
                    const SizedBox(height: 8),
                    SegmentedButton<Role>(
                      segments: const [
                        ButtonSegment(value: Role.patient, label: Text('Patient'), icon: Icon(Icons.person)),
                        ButtonSegment(value: Role.doctor, label: Text('Doctor'), icon: Icon(Icons.medical_services)),
                        ButtonSegment(value: Role.student, label: Text('Student'), icon: Icon(Icons.school)),
                      ],
                      selected: {_role},
                      onSelectionChanged: (selection) => setState(() => _role = selection.first),
                    ),
                    if (_role == Role.doctor) ...[
                      const SizedBox(height: 8),
                      const Text('A doctor account must be verified by an administrator before comparing models.'),
                    ],
                    const SizedBox(height: 24),
                    FilledButton(
                      onPressed: _busy ? null : _register,
                      child: _busy
                          ? const SizedBox(height: 20, width: 20, child: CircularProgressIndicator(strokeWidth: 2))
                          : const Text('Create account'),
                    ),
                  ],
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}
