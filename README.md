# Samsung SDK Build Tool

This simple program will allow you to quickly build Samsung App Packges for convenient iterative testing.

This program may be compiled into a JAR and used directly in your IDE of choice. For example, as a custom Builder in Eclipse.

When using this as a JAR Builder, these arguments might be useful as a template when targetting your java executable:

```
-jar ${workspace_loc:/ProjectName/sdkb.jar}
-i D:\Repos\SamsungProject
-w D:\www
-v 1.0
-n AppName
-r Europe
-W AppId
-I 192.168.0.2
-D "AppName description"
-V
```

Uses libraries:

* args4j - Parse command line arguments
* Apache Commons IO - Trusted efficient File copy methods
* YUI Compressor - Optional script compression / obfuscation