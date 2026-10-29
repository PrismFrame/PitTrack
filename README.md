
# 🔧 PitTrack

### Robotics Inventory Management for FIRST Robotics Competition Teams

**PitTrack** is a web-based inventory management application designed specifically for FIRST Robotics Competition (FRC) teams. It helps teams organize, track, and manage their robotics equipment, components, tools, and resources in one centralized location.

From motors and electronics to spare parts and competition equipment, PitTrack aims to simplify inventory management so teams can spend less time searching for equipment and more time building robots.

---

## 🎯 The Problem

FRC teams work with hundreds of components, tools, and pieces of equipment throughout the build and competition seasons.

Managing this inventory can quickly become challenging, especially when equipment is shared between multiple team members and workspace areas.

Common challenges include:

- Losing track of equipment and spare parts.
- Not knowing how many components are available.
- Difficulty locating specific tools or materials.
- Keeping inventory information organized and up to date.
- Managing shared resources across a robotics team.

**PitTrack addresses these challenges** by providing a centralized inventory management system where each robotics team can independently manage its own equipment and resources.

---

## ✨ Features

### 👥 Team Management
- Create individual accounts for robotics teams.
- Secure team authentication.
- Team-specific inventory storage.
- Account logout and deletion.

### 📦 Inventory Management
- Add new equipment and inventory items.
- Edit existing inventory information.
- Delete items that are no longer needed.
- Store detailed equipment information, including:
  - Name and description
  - Quantity
  - SKU
  - Price
  - Storage location

### 🔍 Inventory Search
- Search inventory by equipment name.
- Find items using their SKU.
- Search by storage location.
- Quickly locate specific components and equipment.

### 📊 Stock Monitoring
- Identify items currently in stock.
- Track low-stock inventory.
- Identify out-of-stock items.
- Keep track of available quantities.

---

## 🛠️ Technology Stack

PitTrack is being developed using Java and the Spring ecosystem, with PostgreSQL for persistent data storage.

| Technology | Purpose |
|---|---|
| Java | Backend programming language |
| Spring Boot | Application framework |
| Spring Security | Authentication and security |
| Spring Data JPA | Database access and persistence |
| Hibernate | Object-relational mapping |
| PostgreSQL | Relational database |
| Thymeleaf | Server-side HTML templating |
| HTML / CSS / JavaScript | Frontend development |
| JUnit | Application testing |
| Git / GitHub | Version control and collaboration |

---

## 🚧 Project Status

**Currently in Development**

PitTrack is an ongoing personal software development project. Its core functionality is currently being implemented, with the goal of creating a practical and reliable inventory management solution for FRC teams.

### Implemented Functionality
- [x] Team registration
- [x] User authentication
- [x] Team-specific inventory storage
- [x] Add inventory items
- [x] Update inventory items
- [x] Delete inventory items
- [x] Inventory management
- [x] Team account deletion

### Planned Features
- [ ] Equipment checkout and return tracking
- [ ] Maintenance records and repair history
- [ ] Inventory activity logs
- [ ] Improved inventory reporting
- [ ] Additional tools for managing robotics team resources

---

## 💡 Development Goals

PitTrack is also a personal learning project focused on developing practical software engineering skills through building a real-world application.

Key areas of learning include:

- **Backend Development:** Building applications with Java and Spring Boot.
- **Database Design:** Designing relational databases and implementing team-based data isolation.
- **Authentication & Authorization:** Implementing secure account management with Spring Security.
- **Application Architecture:** Understanding CRUD operations, MVC architecture, and RESTful concepts.
- **Software Testing:** Writing tests, debugging issues, and improving application reliability.
- **Version Control:** Using Git and GitHub to manage development.
- **Deployment & Maintenance:** Learning how to deploy, maintain, and improve a production-style application.

---

## 🤖 Acknowledgments

PitTrack was developed independently with assistance from **ChatGPT by OpenAI**.

ChatGPT was used as a development resource for:
- Programming guidance and explanations.
- Debugging and troubleshooting.
- Understanding Java and Spring Boot concepts.
- Database design recommendations.
- Software architecture and development suggestions.

The project remains an independent learning endeavor, with the goal of developing practical engineering skills while addressing an actual organizational need within the FRC robotics community.

---

<p align="center">
  <b>Built for robotics teams. Designed to keep inventory on track.</b>
  <br>
  🔧 PitTrack
</p>
