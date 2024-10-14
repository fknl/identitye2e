package com.indentitye2e.library.service;

import com.indentitye2e.library.model.Book;
import com.indentitye2e.library.repository.LibraryCrud;
import com.indentitye2e.library.utility.LibraryCache;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class LibraryService {

    private final LibraryCrud libraryCrud;

    private final LibraryCache libraryCache;

    private final Object lock = new Object();

    public LibraryService(LibraryCrud libraryCrud, LibraryCache libraryCache) {
        this.libraryCrud = libraryCrud;
        this.libraryCache = libraryCache;
    }

    public Book addBook(Book book) {
        checkBookNotExists(book.getISBN());
        synchronized (lock) {
            checkBookNotExists(book.getISBN());
            libraryCache.addCache(book.getISBN(), book);
            return libraryCrud.save(book);
        }
    }

    public void removeBook(String isbn) {
        checkBookExist(isbn);
        synchronized (lock) {
            checkBookExist(isbn);
            libraryCache.removeCache(isbn);
            libraryCrud.deleteById(isbn);
        }
    }

    public Book borrowBook(String isbn) {
        checkBookExist(isbn);
        synchronized (lock) {
            Book book = checkBookExist(isbn);
            if (book.getAvailablecopies() > 0) {
                book.setAvailablecopies(book.getAvailablecopies() - 1);
                libraryCache.addCache(book.getISBN(), book);
                return libraryCrud.save(book);
            } else {
                throw new ResponseStatusException(HttpStatus.CONFLICT);
            }
        }
    }

    public Book returnBook(String isbn) {
        checkBookExist(isbn);
        synchronized (lock) {
            Book book = checkBookExist(isbn);
            book.setAvailablecopies(book.getAvailablecopies() + 1);
            libraryCache.addCache(book.getISBN(), book);
            return libraryCrud.save(book);
        }
    }

    public Book findBookByISBN(String isbn) {
        if (libraryCache.getCache(isbn) == null) {
            Optional<Book> bookOptional = libraryCrud.findById(isbn);
            if (bookOptional.isPresent()) {
                Book book = bookOptional.get();
                libraryCache.addCache(book.getISBN(), book);
                return book;
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }
        } else {
            return libraryCache.getCache(isbn);
        }
    }

    public List<Book> findByAuthor(String author) {
        return libraryCrud.findByAuthor(author);
    }

    private void checkBookNotExists(String isbn) {
        if (libraryCrud.findById(isbn).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
    }

    private Book checkBookExist(String isbn) {
        return libraryCrud.findById(isbn).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}


