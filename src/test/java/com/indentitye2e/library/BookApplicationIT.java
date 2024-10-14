package com.indentitye2e.library;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.indentitye2e.library.model.Book;
import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookApplicationIT {

    TestRestTemplate restTemplate = new TestRestTemplate();
    HttpHeaders headers = new HttpHeaders();
    ObjectMapper objectMapper = new ObjectMapper();
    @LocalServerPort
    private int port;

    @Test
    public void testAddingBook() throws JSONException, JsonProcessingException {

        cleanDB();

        Book book = new Book("1", "Test", "Ufuk", 2020, 10);
        HttpEntity<Book> entity = new HttpEntity<>(book, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort("/book"),
                HttpMethod.POST, entity, String.class);

        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());

        /*
        Create Test
         */
        entity = new HttpEntity<>(null, headers);
        response = restTemplate.exchange(
                createURLWithPort("/book/isbn/1"),
                HttpMethod.GET, entity, String.class);

        Book result = objectMapper.readValue(response.getBody(), Book.class);

        Assertions.assertEquals(result.getISBN(), book.getISBN());
        Assertions.assertEquals(result.getAuthor(), book.getAuthor());
        Assertions.assertEquals(result.getTitle(), book.getTitle());
        Assertions.assertEquals(result.getAvailablecopies(), book.getAvailablecopies());
        Assertions.assertEquals(result.getPublicationyear(), book.getPublicationyear());


        /*
        Return 2 book Test
         */
        restTemplate.exchange(
                createURLWithPort("/book/return/1"),
                HttpMethod.PUT, entity, String.class);
        restTemplate.exchange(
                createURLWithPort("/book/return/1"),
                HttpMethod.PUT, entity, String.class);

        response = restTemplate.exchange(
                createURLWithPort("/book/isbn/1"),
                HttpMethod.GET, entity, String.class);

        result = objectMapper.readValue(response.getBody(), Book.class);

        Assertions.assertEquals(result.getISBN(), book.getISBN());
        Assertions.assertEquals(result.getAuthor(), book.getAuthor());
        Assertions.assertEquals(result.getTitle(), book.getTitle());
        Assertions.assertEquals(result.getAvailablecopies(), book.getAvailablecopies() + 2);
        Assertions.assertEquals(result.getPublicationyear(), book.getPublicationyear());


        /*
        Borrow 1 book Test
         */
        restTemplate.exchange(
                createURLWithPort("/book/borrow/1"),
                HttpMethod.PUT, entity, String.class);

        response = restTemplate.exchange(
                createURLWithPort("/book/isbn/1"),
                HttpMethod.GET, entity, String.class);

        result = objectMapper.readValue(response.getBody(), Book.class);

        Assertions.assertEquals(result.getISBN(), book.getISBN());
        Assertions.assertEquals(result.getAuthor(), book.getAuthor());
        Assertions.assertEquals(result.getTitle(), book.getTitle());
        Assertions.assertEquals(result.getAvailablecopies(), book.getAvailablecopies() + 1);
        Assertions.assertEquals(result.getPublicationyear(), book.getPublicationyear());

        /*
        Find by author test
         */

        Book bookTheSecond = new Book("2", "Test 2", "Ufuk", 2222, 5);
        entity = new HttpEntity<>(bookTheSecond, headers);

        response = restTemplate.exchange(
                createURLWithPort("/book"),
                HttpMethod.POST, entity, String.class);

        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());

        response = restTemplate.exchange(
                createURLWithPort("/book/author/Ufuk"),
                HttpMethod.GET, entity, String.class);

        List<Book> resultList = objectMapper.readValue(response.getBody(), new TypeReference<List<Book>>(){});

        Assertions.assertEquals(2, resultList.size());
    }


    @Test
    void multiThreadTest() throws BrokenBarrierException, InterruptedException, JsonProcessingException {

        cleanDB();

        Book book = new Book("1", "Test", "Ufuk", 2020, 10);
        HttpEntity<Book> entity = new HttpEntity<>(book, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort("/book"),
                HttpMethod.POST, entity, String.class);

        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());


        CountDownLatch latchForBorrow = new CountDownLatch(15);
        CountDownLatch latchForReturn = new CountDownLatch(10);

        Thread threadBorrow = new Thread(() -> {
            HttpEntity<Book> entityThread = new HttpEntity<>(null, headers);

            for (int i = 0; i < 15; i++) {
                restTemplate.exchange(
                        createURLWithPort("/book/borrow/1"),
                        HttpMethod.PUT, entityThread, String.class);
                latchForBorrow.countDown();
            }
        });
        Thread threadReturn = new Thread(() -> {
            HttpEntity<Book> entityThread = new HttpEntity<>(null, headers);

            for (int i = 0; i < 10; i++) {
                restTemplate.exchange(
                        createURLWithPort("/book/return/1"),
                        HttpMethod.PUT, entityThread, String.class);
                latchForReturn.countDown();
            }
        });


        threadBorrow.start();
        threadReturn.start();
        latchForBorrow.await();
        latchForReturn.await();

        response = restTemplate.exchange(
                createURLWithPort("/book/isbn/1"),
                HttpMethod.GET, entity, String.class);

        Book result = objectMapper.readValue(response.getBody(), Book.class);

        Assertions.assertEquals(result.getISBN(), book.getISBN());
        Assertions.assertEquals(result.getAuthor(), book.getAuthor());
        Assertions.assertEquals(result.getTitle(), book.getTitle());
        Assertions.assertEquals(result.getAvailablecopies(), 5);
        Assertions.assertEquals(result.getPublicationyear(), book.getPublicationyear());

    }

    private String createURLWithPort(String uri) {
        return "http://localhost:" + port + uri;
    }

    private void cleanDB() {
        HttpEntity<Book> entity = new HttpEntity<>(null, headers);

        restTemplate.exchange(
                createURLWithPort("/book/1"),
                HttpMethod.DELETE, entity, String.class);
        restTemplate.exchange(
                createURLWithPort("/book/2"),
                HttpMethod.DELETE, entity, String.class);
    }
}
