Currently working from (functional basics):
http://eed3si9n.com/herding-cats/

TODO / TO READ:
https://typelevel.org/cats/faq.html

https://www.scalawithcats.com/dist/scala-with-cats.pdf


To run a single test in a spec use (Full spec description from test is not needed, just enough for it to be unique)  e.g.

testOnly akka.streams.SourceSpec -- -z "use Source.unfoldResourceAsync"