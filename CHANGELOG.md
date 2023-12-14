
## 0.4.4-SNAPSHOT - pending


## 0.4.3 - 2023-12-14

*	fix: handle any cache access failures
*	build: update dependencies

## 0.4.2 - 2023-09-29

*	fix: handle case when cached key/value are obsolete after refactoring
*	fix: handle case when @Cacheable method have no argument
*	fix: handle generic return type for cache key
*	build: update dependencies

## 0.4.1 - 2022-02-14

*	fix: add baseUrl in cache key hashing
*	fix: add param & argument sep for key
*	build: update dependencies

## 0.4.0 - 2022-01-20

*	add internal cache manager definition
*	add cache manager provider interface
*	add spring framework cache manager support
*	build: support jdk-17
*	build: update dependencies
*	test: update to junit5

## 0.3.1 - 2021-01-22

*	fix: NPE on logger when post body is empty
*	build: update dependencies
*	add RequestInterceptor to FutureFit builder
*	add DefaultExceptionInterceptor with compiled Url

## 0.3.0 - 2020-05-17

*	build: update dependencies
*	add custom logging implementation (based on retrofit1 implementation)
*	add user agent interceptor with builder settings
*	add default okhttp3 cookieJar
*	remove Futurefit 1 implementation

## 0.2.1 - 25/09/2019

*	add OkHttp logging dependency
*	update dependencies

## 0.2.0 - 02/08/2019

*	feat: support for retrofit2
*	deprecated: futurefit is deprecated, use futurefit2 instead

## 0.1.4 and before

*	support of retrofit1

