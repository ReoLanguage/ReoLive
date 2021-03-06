addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.8.2")

//addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.29")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.4.0")

// only works with older scalajs (0.6.29) - to help developing JS
//addSbtPlugin("com.lihaoyi" % "workbench" % "0.4.1")

// using maven in treo sub-module
//addSbtPlugin("com.github.shivawu" % "sbt-maven-plugin" % "0.1.2")

logLevel := Level.Warn

resolvers += Resolver.sbtPluginRepo("releases")


//resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
//
//resolvers += "Typesafe Simple Repository" at
//  "http://repo.typesafe.com/typesafe/simple/maven-releases/"

