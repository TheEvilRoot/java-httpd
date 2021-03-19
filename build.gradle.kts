plugins {
    java
    `maven-publish`
}

group = "me.theevilroot"
version = "0.3"

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "me.theevilroot"
            artifactId = "httpd"
            version = version
            from(components["java"])

            pom {
                name.set("JavaHTTPd Library")
                description.set("Simple HTTP server library for java and kotlin")
                url.set("http://github.com/TheEvilRoot/httpd")
                developers {
                    developer {
                        id.set("theevilroot")
                        name.set("TheEvilRoot")
                        email.set("robot-creatix@ya.ru")
                    }
                }
            }
        }
    }
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
}