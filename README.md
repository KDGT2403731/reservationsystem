<h1>予約管理システムアプリケーション</h1>
** 予約管理システムアプリケーション **は、予約管理やシフト管理を目的にしたアプリケーションです。

## ディレクトリ構成
```
.
├── bin
│   ├── main
│   │   ├── application.properties
│   │   ├── com
│   │   │   └── example
│   │   │       └── reservationsystem
│   │   │           ├── config
│   │   │           │   └── SecurityConfig.class
│   │   │           ├── controller
│   │   │           │   ├── AdminController.class
│   │   │           │   ├── DashboardController.class
│   │   │           │   ├── LoginController.class
│   │   │           │   ├── RegisterController.class
│   │   │           │   ├── ReservationController.class
│   │   │           │   ├── StaffController.class
│   │   │           │   ├── WaitlistController.class
│   │   │           │   └── WaitlistController$WaitlistStats.class
│   │   │           ├── entity
│   │   │           │   ├── Reservation.class
│   │   │           │   ├── Shift.class
│   │   │           │   ├── User.class
│   │   │           │   └── Waitlist.class
│   │   │           ├── repository
│   │   │           │   ├── ReservationRepository.class
│   │   │           │   ├── ShiftRepository.class
│   │   │           │   ├── UserRepository.class
│   │   │           │   └── WaitlistRepository.class
│   │   │           ├── ReservationsystemApplication.class
│   │   │           └── service
│   │   │               ├── ReservationService.class
│   │   │               ├── ShiftService.class
│   │   │               └── WaitlistService.class
│   │   ├── data.sql
│   │   ├── schema.sql
│   │   ├── static
│   │   │   └── css
│   │   │       └── style.css
│   │   └── templates
│   │       ├── admin_dashboard.html
│   │       ├── admin_reservations.html
│   │       ├── admin_shifts.html
│   │       ├── admin_statistics.html
│   │       ├── customer_dashboard.html
│   │       ├── index.html
│   │       ├── login.html
│   │       ├── register.html
│   │       ├── reservation_form.html
│   │       ├── reservation_history.html
│   │       ├── staff_dashboard.html
│   │       ├── staff_reservation_form.html
│   │       ├── staff_reservations.html
│   │       ├── staff_shift_management.html
│   │       ├── staff_waitlist_management.html
│   │       └── waitlist_register.html
│   └── test
│       └── com
│           └── example
│               └── reservationsystem
│                   └── ReservationsystemApplicationTests.class
├── build
│   ├── classes
│   │   └── java
│   │       └── main
│   │           └── com
│   │               └── example
│   │                   └── reservationsystem
│   │                       ├── config
│   │                       │   └── SecurityConfig.class
│   │                       ├── controller
│   │                       │   ├── AdminController.class
│   │                       │   ├── DashboardController.class
│   │                       │   ├── LoginController.class
│   │                       │   ├── RegisterController.class
│   │                       │   ├── ReservationController.class
│   │                       │   ├── StaffController.class
│   │                       │   ├── WaitlistController.class
│   │                       │   └── WaitlistController$WaitlistStats.class
│   │                       ├── entity
│   │                       │   ├── Reservation.class
│   │                       │   ├── Shift.class
│   │                       │   ├── User.class
│   │                       │   └── Waitlist.class
│   │                       ├── repository
│   │                       │   ├── ReservationRepository.class
│   │                       │   ├── ShiftRepository.class
│   │                       │   ├── UserRepository.class
│   │                       │   └── WaitlistRepository.class
│   │                       ├── ReservationsystemApplication.class
│   │                       └── service
│   │                           ├── ReservationService.class
│   │                           ├── ShiftService.class
│   │                           └── WaitlistService.class
│   ├── generated
│   │   └── sources
│   │       ├── annotationProcessor
│   │       │   └── java
│   │       │       └── main
│   │       └── headers
│   │           └── java
│   │               └── main
│   ├── libs
│   │   └── app.jar
│   ├── reports
│   │   └── problems
│   │       └── problems-report.html
│   ├── resolvedMainClassName
│   ├── resources
│   │   └── main
│   │       ├── application.properties
│   │       ├── data.sql
│   │       ├── schema.sql
│   │       ├── static
│   │       │   └── css
│   │       │       └── style.css
│   │       └── templates
│   │           ├── admin_dashboard.html
│   │           ├── admin_reservations.html
│   │           ├── admin_shifts.html
│   │           ├── admin_statistics.html
│   │           ├── customer_dashboard.html
│   │           ├── index.html
│   │           ├── login.html
│   │           ├── register.html
│   │           ├── reservation_form.html
│   │           ├── reservation_history.html
│   │           ├── staff_dashboard.html
│   │           ├── staff_reservation_form.html
│   │           ├── staff_reservations.html
│   │           ├── staff_shift_management.html
│   │           ├── staff_waitlist_management.html
│   │           └── waitlist_register.html
│   └── tmp
│       ├── bootJar
│       │   └── MANIFEST.MF
│       └── compileJava
│           └── previous-compilation-data.bin
├── build.gradle
├── gradle
│   └── wrapper
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── gradlew
├── gradlew.bat
├── HELP.md
├── README.md
├── settings.gradle
└── src
    ├── main
    │   ├── java
    │   │   └── com
    │   │       └── example
    │   │           └── reservationsystem
    │   │               ├── config
    │   │               │   └── SecurityConfig.java
    │   │               ├── controller
    │   │               │   ├── AdminController.java
    │   │               │   ├── DashboardController.java
    │   │               │   ├── LoginController.java
    │   │               │   ├── RegisterController.java
    │   │               │   ├── ReservationController.java
    │   │               │   ├── StaffController.java
    │   │               │   └── WaitlistController.java
    │   │               ├── entity
    │   │               │   ├── Reservation.java
    │   │               │   ├── Shift.java
    │   │               │   ├── User.java
    │   │               │   └── Waitlist.java
    │   │               ├── repository
    │   │               │   ├── ReservationRepository.java
    │   │               │   ├── ShiftRepository.java
    │   │               │   ├── UserRepository.java
    │   │               │   └── WaitlistRepository.java
    │   │               ├── ReservationsystemApplication.java
    │   │               └── service
    │   │                   ├── ReservationService.java
    │   │                   ├── ShiftService.java
    │   │                   └── WaitlistService.java
    │   └── resources
    │       ├── application.properties
    │       ├── data.sql
    │       ├── schema.sql
    │       ├── static
    │       │   └── css
    │       │       └── style.css
    │       └── templates
    │           ├── admin_dashboard.html
    │           ├── admin_reservations.html
    │           ├── admin_shifts.html
    │           ├── admin_statistics.html
    │           ├── customer_dashboard.html
    │           ├── index.html
    │           ├── login.html
    │           ├── register.html
    │           ├── reservation_form.html
    │           ├── reservation_history.html
    │           ├── staff_dashboard.html
    │           ├── staff_reservation_form.html
    │           ├── staff_reservations.html
    │           ├── staff_shift_management.html
    │           ├── staff_waitlist_management.html
    │           └── waitlist_register.html
    └── test
        └── java
            └── com
                └── example
                    └── reservationsystem
                        └── ReservationsystemApplicationTests.java
```
