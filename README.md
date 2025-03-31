# Flight Planner

## Development process
The development of the backend and frontend took about 30 hours. Most of the time spent went on configuring amadeus - a flight booking API. Since there were unforeseen limitations,
and a lot of spaghetti code, I decided to remove it and make a simpler version. Using amadeus would have been beneficial, if I would have hosted the backend from a public IP
and kept the database running. The main reason was that the fetching of data took too much resources and time, since the backend had to process through too many destinations and dates.

## 📌 Project Overview
Flight Planner is a web application that allows users to search for flights, select seats, and book tickets. It consists of a **Spring Boot backend** and a **Vue.js frontend**.

---

## 🚀 Prerequisites
Ensure you have the following installed:
- **Java 21+**
- **Gradle**
- **Node.js & npm**
- **Docker (Optional, for containerized setup)**
- **PostgreSQL (Optional, if not using Docker)**

---

## 🔧 Backend Setup (Spring Boot)

### 1️⃣ Clone the Repository
```sh
 git clone https://github.com/your-repo/flight-planner.git
 cd flight-planner/backend
```

### 2️⃣ Run docker-compose
```sh
docker-compose up --build -d
```

### 3️⃣ Build & Run the Backend
```sh
./gradlew build
./gradlew bootRun
```
Backend runs at **http://localhost:8080**

---

## 🎨 Frontend Setup (Vue.js)

### 1️⃣ Navigate to Frontend Directory
```sh
cd ../frontend
```

### 2️⃣ Install Dependencies
```sh
npm install
```

### 3️⃣ Start Development Server
```sh
npm run dev
```
Frontend runs at **http://localhost:8081**
---

## 🔄 API Endpoints
| Method | Endpoint                     | Description                             |
|--------|------------------------------|-----------------------------------------|
| `GET`  | `/flight/fetch`              | Get all flights                         |
| `GET`  | `/flight/generate`           | Generate random flights                 |
| `GET`  | `/flight/{flightId}/seats`   | Find seats a flight                     |
| `POST` | `/flight/search`             | Filter flights with optional parameters |
| `PUT`  | `/flight/{flightId}/booking` | Book selected seats                     |

---

## 📢 Notes
- Adjust **CORS settings** in Spring Boot if needed.
