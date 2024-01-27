# Simbot4 Multiplatform component template

这是一个基于 simbot4 的**组件库开发**项目模板。

再强调一下这是组件库开发，不是普通应用开发。

此模板基于 Kotlin/Multiplatform，你也可以简单的将其更替为 Kotlin/JVM。

## 参考内容

请参考 `src/commonMain/kotlin` 中的源代码大部分详细信息都会使用注释说明。

### FooComponent

一个实例组件的**组件标识**，算是实现一个组件库的最初工作，不过在一些极为简单的场景下也可以不实现。

### FooPlugin

一个实例组件的**插件**，一般用于提供一些简单的、或与 bot 功能无关的内容。
如果功能与 bot 相关，更建议参考 `FooBotManager` 相关内容。

### FooBotManager、FooBot 

在包路径 `com.example.component.foo.bot` 下，

是一些与 bot 相关功能的实现示例。

### FooEvent

在包路径 `com.example.component.foo.event` 下，

是一些组件库中实现标准事件类型的示例。

## 移除不需要内容

当你准备移除一些你不需要的内容时，
记得也要去
`jvmMain/resources/META-INF/services/xxx`
中对应的文件中删除对应的引用。
