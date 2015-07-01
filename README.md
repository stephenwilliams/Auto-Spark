Auto Spark
----------

Auto Spark is an alternative way to use [Spark][spark]. This example assumes you have a basic understanding of how to use Spark.

###Downloads

**Maven**

```xml
<dependency>
	<groupId>com.alta189</groupId>
	<artifactId>auto-spark</artifactId>
	<version>1.1</version>
</dependency>
```

**Gradle**

```gradle
'com.alta189:auto-spark:1.1'
```

You can also download the jars [here][releases].

###Getting Started

ExampleServer.java

```java
import com.alta189.auto.spark.AutoSpark;

public class ExampleServer {
	public static void main(String[] args) {
		new AutoSpark().run();
	}
}
```

HelloWorld.java

```java
import com.alta189.auto.spark.Controller;
import com.alta189.auto.spark.ResourceMapping;
import spark.Request;
import spark.Response;

@Controller
public class HelloWorld {
	@ResourceMapping("/hello")
	public String hello(Request request, Response response) {
		return "Hello, World!";
	}
}
```

View at: http://localhost:4567/hello

###Examples

ExampleController.java

```java
import com.alta189.auto.spark.Controller;
import com.alta189.auto.spark.RequestMethod;
import com.alta189.auto.spark.ResourceMapping;
import spark.Request;
import spark.Response;

@Controller
public class ExampleController {
	@ResourceMapping(value = "/users/add", method = RequestMethod.PUT)
	public String addUser(Request request, Response response) {
		// Internal Logic
	}
	
	@ResourceMapping(value = "/users/update", method = RequestMethod.POST)
	public String updateUser(Request request, Response response) {
		// Internal Logic
	}
	
	@ResourceMapping(value = "/users/delete", method = RequestMethod.DELETE)
	public String deleteUser(Request request, Response response) {
		// Internal Logic
	}
	
	@ResourceMapping(value = "/users/list", method = RequestMethod.GET)
	public String listUsers(Request request, Response response) {
		// Internal Logic
	}
}
```

You could also simplify each methods ```@ResourceMapping``` by removing the ```/users``` from each and adding ```@ResourceMapping("/users")``` to the class.


----


Result.java

```java
import com.alta189.auto.spark.AutoSparkUtils;
import com.alta189.auto.spark.SparkResponseTransformer;

public class Result {
	private final String result;

	public Result(String result) {
		this.result = result;
	}

	public String getResult() {
		return "{ \"result\" : \"" + result + "\" }";
	}

	public static class ResultTransformer extends SparkResponseTransformer {
		@Override
		public String render(Object model) throws Exception {
			Result result = AutoSparkUtils.safeCast(model);
			if (result == null) {
				throw new IllegalAccessException("result null");
			}
			return result.getResult();
		}
	}
}
```

TransformerExample.java

```java
import com.alta189.auto.spark.Controller;
import com.alta189.auto.spark.ResourceMapping;
import com.alta189.auto.spark.Transformer;
import spark.Request;
import spark.Response;

@Controller
public class TransformerExample {
	@Transformer(Result.ResultTransformer.class)
	@ResourceMapping("/status")
	public Result status(Request request, Response response) {
		return new Result("online");
	}

	@Transformer(Result.ResultTransformer.class)
	@ResourceMapping("/health")
	public Result health(Request request, Response response) {
		return new Result("good");
	}
	
	@ResourceMapping("/hello")
	public String hello(Request request, Response response) {
		return "Hello, World!";
	}
}

```

You can simplify this class by declaring the Transformer to be for the entire class and setting the ```/hello``` resource to ignore the parent transformer as seen below

```java
import com.alta189.auto.spark.Controller;
import com.alta189.auto.spark.ResourceMapping;
import com.alta189.auto.spark.Transformer;
import spark.Request;
import spark.Response;

@Controller
@Transformer(Result.ResultTransformer.class)
public class TransformerExample {
	@ResourceMapping("/status")
	public Result status(Request request, Response response) {
		return new Result("online");
	}
	
	@ResourceMapping("/health")
	public Result health(Request request, Response response) {
		return new Result("good");
	}
	
	@Transformer(ignoreParent = true)
	@ResourceMapping("/hello")
	public String hello(Request request, Response response) {
		return "Hello, World!";
	}
}
```

---

hello.ftl

```html
<h1>Hello, ${message}!</h1>
```

TemplateEngineExample.java

```java
import com.alta189.auto.spark.ResourceMapping;
import com.alta189.auto.spark.TemplateEngine;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class TemplateEngineExample {
	@ResourceMapping("/hello/:message")
	@TemplateEngine(FreeMarkerEngine.class)
	public ModelAndView template(Request request, Response response) {
		Map<String, Object> attributes = new HashMap<>();
		String message = request.params("message");
		if (StringUtils.isEmpty(message)) {
			message = "World";
		}
		attributes.put("message", message);
		return new ModelAndView(attributes, "hello.ftl");
	}
}
```

Just like ```@Transformer``` a template engine can be set at the class level and then ignored at the method level.


---


ExceptionExample.java

```java
import com.alta189.auto.spark.Controller;
import com.alta189.auto.spark.ExceptionMapping;
import spark.Request;
import spark.Response;

@Controller
public class ExceptionExample {
	@ExceptionMapping(NotFoundException.class)
	public void notFound(Request request, Response response) {
		response.status(404);
		response.body("Resource not found");
	}

	@ExceptionMapping(NullPointerException.class)
	public void notFound(Request request, Response response) {
		response.status(404);
		response.body("Oops! Looks like something was broken!");
	}
}
```


---


FilterExample.java

```java
import com.alta189.auto.spark.SparkFilter;
import spark.Request;
import spark.Response;

public class FilterExample extends SparkFilter {
	@Override
	public void before(Request request, Response response) {
		// Before the Request is handled
	}

	@Override
	public void after(Request request, Response response) {
		// After the Request is handled
	}
}
```

Adding ```@FilterMapping("/api/*")``` to the Class would limit the filter to only paths starting with ```/api/```.


[spark]: http://sparkjava.com
[releases]: https://github.com/alta189/Auto-Spark/releases
