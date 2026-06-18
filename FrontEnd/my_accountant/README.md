# my_accountant

A new Flutter project.

## Getting Started

This project is a starting point for a Flutter application.

A few resources to get you started if this is your first Flutter project:

- [Learn Flutter](https://docs.flutter.dev/get-started/learn-flutter)
- [Write your first Flutter app](https://docs.flutter.dev/get-started/codelab)
- [Flutter learning resources](https://docs.flutter.dev/reference/learning-resources)

For help getting started with Flutter development, view the
[online documentation](https://docs.flutter.dev/), which offers tutorials,
samples, guidance on mobile development, and a full API reference.



lib/
│
├── main.dart
│
├── core/
│   ├── constants/
│   │   ├── app_colors.dart
│   │   ├── app_strings.dart
│   │   ├── app_assets.dart
│   │   └── api_constants.dart
│   │
│   ├── theme/
│   │   ├── app_theme.dart
│   │   ├── light_theme.dart
│   │   └── dark_theme.dart
│   │
│   ├── network/
│   │   ├── dio_client.dart
│   │   ├── api_service.dart
│   │   └── interceptor.dart
│   │
│   ├── storage/
│   │   ├── shared_pref_service.dart
│   │   ├── secure_storage.dart
│   │   └── local_database.dart
│   │
│   ├── utils/
│   │   ├── validators.dart
│   │   ├── formatter.dart
│   │   ├── helper.dart
│   │   └── extensions.dart
│   │
│   └── widgets/
│       ├── app_button.dart
│       ├── app_textfield.dart
│       ├── loading_widget.dart
│       └── custom_card.dart
│
├── routes/
│   ├── app_routes.dart
│   └── route_generator.dart
│
├── services/
│   ├── notification_service.dart
│   ├── firebase_service.dart
│   ├── file_upload_service.dart
│   └── share_service.dart
│
├── features/
│
│   ├── authentication/
│   │
│   │   ├── data/
│   │   │   ├── datasource/
│   │   │   ├── models/
│   │   │   └── repository_impl/
│   │   │
│   │   ├── domain/
│   │   │   ├── entities/
│   │   │   ├── repository/
│   │   │   └── usecases/
│   │   │
│   │   └── presentation/
│   │       ├── screens/
│   │       ├── widgets/
│   │       └── provider/
│
│   ├── dashboard/
│   │
│   │   ├── data/
│   │   ├── domain/
│   │   └── presentation/
│   │       ├── screens/
│   │       │   └── dashboard_screen.dart
│   │       ├── widgets/
│   │       └── provider/
│
│   ├── expenses/
│   │
│   │   ├── data/
│   │   │   ├── datasource/
│   │   │   ├── models/
│   │   │   │   └── expense_model.dart
│   │   │   └── repository_impl/
│   │   │
│   │   ├── domain/
│   │   │   ├── entities/
│   │   │   │   └── expense_entity.dart
│   │   │   ├── repository/
│   │   │   └── usecases/
│   │   │
│   │   └── presentation/
│   │       ├── screens/
│   │       │   ├── expense_list_screen.dart
│   │       │   ├── add_expense_screen.dart
│   │       │   └── expense_detail_screen.dart
│   │       │
│   │       ├── widgets/
│   │       └── provider/
│
│   ├── income/
│   │   ├── data/
│   │   ├── domain/
│   │   └── presentation/
│
│   ├── savings/
│   │   ├── data/
│   │   ├── domain/
│   │   └── presentation/
│
│   ├── budget/
│   │   ├── data/
│   │   ├── domain/
│   │   └── presentation/
│
│   ├── bills/
│   │   ├── data/
│   │   ├── domain/
│   │   └── presentation/
│
│   ├── analytics/
│   │   ├── data/
│   │   ├── domain/
│   │   └── presentation/
│
│   ├── team/
│   │   ├── data/
│   │   ├── domain/
│   │   └── presentation/
│
│   ├── reports/
│   │   ├── data/
│   │   ├── domain/
│   │   └── presentation/
│
│   ├── notifications/
│   │   ├── data/
│   │   ├── domain/
│   │   └── presentation/
│
│   └── profile/
│       ├── data/
│       ├── domain/
│       └── presentation/
│
├── models/
│   ├── user.dart
│   ├── expense.dart
│   ├── income.dart
│   ├── saving.dart
│   └── team.dart
│
└── generated/