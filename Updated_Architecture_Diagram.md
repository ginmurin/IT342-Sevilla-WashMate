# WashMate - Updated System Architecture

This document contains the updated system architecture diagram for the WashMate project, reflecting the current implementation including the polymorphic payment system, digital wallet, and subscription features.

## PlantUML Source Code

```plantuml
@startuml
!theme cerulean

title WashMate - System Architecture

' Actors
actor "Customer" as customer
actor "Shop Owner" as owner
actor "Administrator" as admin

' Layers
package "Presentation Layer (Frontend)" {
    component "Web Application\n(React / TypeScript)" as web
    component "Mobile Application\n(Native Android)" as mobile
}

package "Application Layer (Backend - Spring Boot)" {
    folder "Authentication & Security" {
        component "Auth Service\n(JWT, Role-Based)" as auth
        component "Google OAuth" as oauth
    }
    
    folder "Core Business Logic" {
        component "Order Service" as order
        component "Service Catalog" as service
        component "Notification Service" as notify
    }
    
    folder "Financial & Subscription" {
        component "Payment Service\n(Polymorphic)" as payment
        component "Wallet Service" as wallet
        component "Subscription Service" as sub
    }
}

package "External Services" {
    component "PayMongo API" as paymongo
    component "Google API" as google_api
}

package "Data Layer (Storage)" {
    database "PostgreSQL\n(Main Database)" as db
    database "Redis\n(Cache & Verification)" as redis
}

' Connections
customer --> web : Uses
customer --> mobile : Uses
owner --> web : Manages Shop
admin --> web : Administers System

web --> auth : API Requests
web --> order
web --> service
web --> payment
web --> wallet
web --> sub

mobile --> auth
mobile --> order
mobile --> wallet

oauth --> google_api : Verifies Token
payment --> paymongo : Processes Payments

auth --> db
order --> db
service --> db
notify --> db
payment --> db
wallet --> db
sub --> db

auth --> redis : Stores OTP/Codes

' Conceptual Data Flow from External Services to Database
paymongo ..> db : Data stored via Payment Service
google_api ..> db : Data stored via Auth Service

@enduml
```

## How to Render

You can render this diagram by:
1. Installing the PlantUML extension in VS Code.
2. Using the online PlantUML viewer at http://www.plantuml.com/plantuml.
3. Using the markdown preview if your environment supports PlantUML rendering.
