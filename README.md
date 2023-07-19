# Поисковый движок по сайтам(SearchEngine)

Данное приложение позволяет индексировать страницы сайтов и осуществлять по ним быстрый поиск необходимой информациии, 
представляет собой веб-сервис, обрабатывающий HTTP-запросы и ответы, 
имеет простой веб-интерфейс для управления и получения результатов запроса.

В конфигурационном файле перед запуском приложения задаются адреса сайтов, по которым приложение осуществлять поиск.
SearchEngine обходит все страницы заданных сайтов и индексирует их.
Для запуска проекта локально вам необходимы JDK от 11 версии, а также необходимо создать локальную базу данных, 
изменив параметры подключения к базе данных в конфигурационном файле <u>application.yaml</u>, такие как:
имя пользователя, пароль и URL-адрес базы данных.

    spring:
        datasource:
            username: username
            password: password
            url: databaseurl

Для работы с веб интерфейсом необходимо в браузере перейти на веб страницу:

    http://localhost:8080/

Теперь веб-интерфейс доступен для работы:
<h2 align="center" style="margin: 0; padding: 0;">

![image](./readme_file/Localhost.PNG )</h2>

Интерфейс главной страницы, на которой выводится информация о индексируемых сайтах:
<h2 align="center" style="margin: 0; padding: 0;">

![image](./readme_file/Dashboard.PNG )</h2>

Поиска на всех сайтах:
<h2 align="center" style="margin: 0; padding: 0;">

![image](./readme_file/Searchallsite.PNG )</h2>

Поиск на определенном сайте:
<h2 align="center" style="margin: 0; padding: 0;">

![image](./readme_file/Searchonesite.PNG )</h2>



# Используемые технологии:
Spring (Boot, MVC, Data), DB MySQL8O, JPA (Hibernate), Apache Lucene , Jsoup, REST, JSON, Lombok, HTML, CSS, JavaScript,
Thymeleaf, jQuery, Maven
