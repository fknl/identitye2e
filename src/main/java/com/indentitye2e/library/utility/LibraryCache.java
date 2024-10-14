package com.indentitye2e.library.utility;

import com.indentitye2e.library.model.Book;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class LibraryCache {

    private final ConcurrentHashMap<String, Book> cache = new ConcurrentHashMap<>();


    public void addCache(String isbn, Book book) {
        cache.put(isbn, book);
    }

    public void removeCache(String isbn) {
        cache.remove(isbn);
    }

    public Book getCache(String isbn) {
        return cache.get(isbn);
    }

}
