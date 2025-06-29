# Telegram Web App User Validation library

:link: [Official documentation: Telegram Mini App Init Data](https://docs.telegram-mini-apps.com/platform/init-data)

## :toolbox: Getting started

### :bangbang: Prerequisites
- Java 11+

## 📦 Package installation
### Maven
```xml
<dependency>
    <groupId>io.github.sanvew</groupId>
    <artifactId>telegram-init-data</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle
```groovy
implementation 'io.github.sanvew:telegram-init-data:1.0.0'
```

```kotlin
// kotlin DSL
implementation("io.github.sanvew:telegram-init-data:1.0.0")
```

## :pencil2: Usage
```java
package com.example;

import io.github.sanvew.tg.init.data.type.InitData;

import static io.github.sanvew.tg.init.data.InitDataUtils.isValid;
import static io.github.sanvew.tg.init.data.InitDataUtils.parse;

public class TestMain {

    public static void main(String[] args) {
        final String botToken = "5768337691:AAH5YkoiEuPk8-FZa32hStHTqXiLPtAEhx8";
        final String initData = "query_id=AAHdF6IQAAAAAN0XohDhrOrc" +
                "&user=%7B%22id%22%3A279058397%2C%22first_name%22%3A%22Vladislav%22%2C%22last_name%22%3A%22Kibenko%22%2C%22username%22%3A%22vdkfrost%22%2C%22language_code%22%3A%22ru%22%2C%22is_premium%22%3Atrue%7D" +
                "&auth_date=1662771648" +
                "&hash=c501b71e775f74ce10e377dea85a7ea24ecd640b223ea86dfe453e0eaed2e2b2";

        System.out.println(isValid(initData, botToken));
        final InitData parsedInitData = parse(initData);
        System.out.println(parsedInitData);
    }
}
```

```kotlin
package com.example

import io.github.sanvew.tg.init.data.InitDataUtils.isValid
import io.github.sanvew.tg.init.data.InitDataUtils.parse
import io.github.sanvew.tg.init.data.type.InitData

fun main(args: Array<String>) {
    val botToken = "5768337691:AAH5YkoiEuPk8-FZa32hStHTqXiLPtAEhx8"
    val initData = "query_id=AAHdF6IQAAAAAN0XohDhrOrc" +
            "&user=%7B%22id%22%3A279058397%2C%22first_name%22%3A%22Vladislav%22%2C%22last_name%22%3A%22Kibenko%22%2C%22username%22%3A%22vdkfrost%22%2C%22language_code%22%3A%22ru%22%2C%22is_premium%22%3Atrue%7D" +
            "&auth_date=1662771648" +
            "&hash=c501b71e775f74ce10e377dea85a7ea24ecd640b223ea86dfe453e0eaed2e2b2"
    
    println(isValid(initData, botToken))

    val parsedInitData: InitData = parse(initData)
    println(parsedInitData)
}
```

## :paperclip: Next releases plans
- [ ] implement [3rd party validation](https://docs.telegram-mini-apps.com/platform/init-data#using-telegram-public-key)

## :pushpin: Fork Notice

This repository is a fork of [tofitaV/TelegramDataValidation](https://github.com/tofitaV/TelegramDataValidation), originally created by [tofitaV](https://github.com/tofitaV). It retains the original MIT license and attribution. Changes and further maintenance by [sanvew](https://github.com/sanvew).
