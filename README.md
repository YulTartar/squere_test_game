# Squares Game API (Spring Boot)

## Описание
Это учебный web-сервис на Java Spring Boot, который реализует игру **"Квадратики"**.  
Сервис принимает состояние игровой доски и вычисляет **следующий ход компьютера**, а также сообщает результат, если игра завершена.

## Технологии
- Java 17
- Spring Boot 3
- REST API (JSON)
- Maven

## Структура проекта
- `GameEngine` — движок игры (логика из Задания 1)
- `GameController` — REST-контроллер
- `BoardDto`, `MoveResultDto` — DTO для входных/выходных данных
- `GlobalExceptionHandler` — обработка ошибок

## Запуск
1. Клонировать репозиторий:
   ```bash
   git clone https://github.com/<ваш_логин>/<>.git
   cd <>
   mvn clean package
   mvn spring-boot:run
   ```

доступен на http://localhost:8080

## API (для Postman / curl)

## Endpoint
```bash
POST /api/{rules}/nextMove
```

- POST /api/squares/nextMove
  {
  "size": 3,
  "data": ".........",
  "nextPlayerColor": "W"
  }
ответ
  {
  "move": {
  "x": 0,
  "y": 0,
  "color": "W"
  },
  "result": null,
  "winner": null,
  "finalBoard": null
  }

