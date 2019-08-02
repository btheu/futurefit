# Futurefit
An extention of the great Retrofit with integration of <a href="https://github.com/btheu/estivate/">Estivate</a>


# Getting Started
```java

    public static void main(String args[]) {
	    Futurefit2 adapter = new Futurefit.Builder()
	    	.baseUrl("https://www.google.com/")
	      .build();
		
	    GoogleApi create = adapter.create(GoogleApi.class);
		
	    String stats = create.search("retrofit").getResultStatistics();
		
	    System.out.println(stats);
	}
	
	public interface GoogleApi {
	    @Estivate
	    @GET("/search?hl=en&safe=off")
	    @Headers({ "User-Agent:Mozilla/5.0 Firefox/68.0" })
	    public Page search(@Query("q") String query);
	}
	
	public class Page {
	    // get the div holding statistics
	    @Text(select="#resultStats")
	    public String resultStatistics;
	}
```

## Download
```xml
<dependency>
	<groupId>com.github.btheu.futurefit</groupId>
	<artifactId>futurefit</artifactId>
	<version>0.2.0</version>
</dependency>
```
