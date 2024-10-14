How to Run

Application uses MongoDb so first need to pull a docker image and run it as a container
* docker pull mongo
* docker run --name mongodb -d mongo

Then for start the application 
* mvn spring-boot:run

After these steps you can run some rest api calls

Example Calls

### For adding a book to library
* url: {POST} localhost:8080/book
* payload: {
  "isbn":"1",
  "title":"MMMMMMMM",
  "author": "ufukunlu",
  "publicationyear": 2222,
  "availablecopies":100
  }

### For search for a book
* url: {GET} localhost:8080/book/isbn/1
* url: {GET} localhost:8080/book/author/ufukunlu

### For borrow for a book
* url: {PUT} localhost:8080/book/borrow/1

### For return for a book
* url: {PUT} localhost:8080/book/return/1

### For delete  a book from library
* url: {DELETE} localhost:8080/book/2

### Run Test
* For unit test: mvn test
* For integration test mvn clean verify

### Design 

* For concurrency issues it can be trusted to database but I didn't prefer it as it is not clear where I will store the data
So I made the implementation on code side
Try to minimize the synchronize blocks as much as possible
Also it is double check before  the synchronize block and inside it to achieve to minimize the time


* It is used a cache mechanism for searching by isbn

* basic rate limiting is added for adding books to the library




