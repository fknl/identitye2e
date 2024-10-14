package com.indentitye2e.library.controller;

import com.indentitye2e.library.model.Book;
import com.indentitye2e.library.service.LibraryService;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.List;

@RestController
public class LibraryController {

    private final LibraryService service;

    private final Bucket bucket;

    public LibraryController(LibraryService service) {
        this.service = service;
        Bandwidth limit = Bandwidth.classic(20, Refill.greedy(20, Duration.ofMinutes(1)));
        this.bucket = Bucket.builder()
                .addLimit(limit)
                .build();
    }

    @PostMapping("/book")
    public Book addBook(@RequestBody Book book) {
        if (bucket.tryConsume(1)) {
            return service.addBook(book);
        } else {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS);
        }
    }

    @DeleteMapping("/book/{isbn}")
    public void removeBook(@PathVariable String isbn) {
        service.removeBook(isbn);
    }

    @GetMapping("/book/isbn/{isbn}")
    public Book findBookByIsbn(@PathVariable String isbn) {
        return service.findBookByISBN(isbn);
    }

    @GetMapping("/book/author/{author}")
    public List<Book> findBookByAuthor(@PathVariable String author) {
        return service.findByAuthor(author);
    }

    @PutMapping("/book/borrow/{isbn}")
    public Book borrowBook(@PathVariable String isbn) {
        return service.borrowBook(isbn);
    }

    @PutMapping("/book/return/{isbn}")
    public Book returnBook(@PathVariable String isbn) {
        return service.returnBook(isbn);
    }
}
