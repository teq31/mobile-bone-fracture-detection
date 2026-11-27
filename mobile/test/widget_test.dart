import 'package:bone_fracture_app/app_scope.dart';
import 'package:bone_fracture_app/main.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';

void main() {
  testWidgets('starts on the login screen', (tester) async {
    SharedPreferences.setMockInitialValues({});

    await tester.pumpWidget(BoneFractureApp(state: AppState()));
    await tester.pumpAndSettle();

    expect(find.text('Bone Fracture Detection'), findsOneWidget);
    expect(find.text('Log in'), findsOneWidget);
    expect(find.text('Create an account'), findsOneWidget);
  });
}
