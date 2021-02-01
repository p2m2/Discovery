addSbtPlugin("org.scala-js"       % "sbt-scalajs"                   % "1.4.0")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject"      % "1.0.0")
addSbtPlugin("org.portable-scala" % "sbt-scala-native-crossproject" % "1.0.0")
addSbtPlugin("org.scala-native"   % "sbt-scala-native"              % "0.3.7")
addSbtPlugin("com.vmunier"        % "sbt-web-scalajs"               % "1.1.0")
addSbtPlugin("com.typesafe.play"  % "sbt-plugin"                    % "2.8.5")
addSbtPlugin("com.typesafe.sbt"   % "sbt-gzip"                      % "1.0.2")
addSbtPlugin("com.typesafe.sbt"   % "sbt-digest"                    % "1.1.4")
addSbtPlugin("org.scoverage"      % "sbt-scoverage"                 % "1.6.1")
addSbtPlugin("com.github.gseitz"  % "sbt-release"                   % "1.0.13")
addSbtPlugin("ch.epfl.scala"      % "sbt-scalajs-bundler"           % "0.20.0")

libraryDependencies += "org.scala-js" %% "scalajs-env-jsdom-nodejs" % "1.1.0"
