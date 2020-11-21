# DiscordCodeFormatter

![code_formatter_demo.gif](code_formatter_demo.gif)

## discord invite link

https://discord.com/api/oauth2/authorize?client_id=779383631255961640&permissions=11328&scope=bot

## Build and run in docker

* replace `change-me` with the jda token in `docker-compose.yml`
* `./mvnw spring-boot:build-image -Pproduction`
* `docker-compose build`
* `docker-compose up -d`
