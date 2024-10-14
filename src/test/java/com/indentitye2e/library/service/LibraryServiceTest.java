package com.indentitye2e.library.service;

import com.indentitye2e.library.model.Book;
import com.indentitye2e.library.repository.LibraryCrud;
import com.indentitye2e.library.utility.LibraryCache;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class LibraryServiceTest {

    @Mock
    private LibraryCrud libraryCrud;

    @InjectMocks
    private LibraryService libraryService;

    @Mock
    private LibraryCache libraryCache;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addBook_success() {
        // Given
        Book book = new Book("1", "Test", "Ufuk", 2020, 10);
        when(libraryCrud.findById("1")).thenReturn(Optional.empty());
        when(libraryCrud.save(any(Book.class))).thenReturn(book);

        // When
        Book addedBook = libraryService.addBook(book);

        // Then
        assertEquals("1", addedBook.getISBN());
        verify(libraryCache, times(1)).addCache(book.getISBN(), book);
        verify(libraryCrud, times(1)).save(book);
    }

    @Test
    void addBook_conflict() {
        // Given
        Book book = new Book("1", "Test", "Ufuk", 2020, 10);
        when(libraryCrud.findById("1")).thenReturn(Optional.of(book)); // Simulate existing book

        //Then
        ResponseStatusException responseStatusException = assertThrows(ResponseStatusException.class, () -> libraryService.addBook(book));

        Assertions.assertTrue(responseStatusException.getStatusCode().is4xxClientError());
        Assertions.assertEquals(responseStatusException.getStatusCode().value(), 409);

        verify(libraryCrud, never()).save(any(Book.class));
        verify(libraryCache, never()).addCache(book.getISBN(), book);
    }

    @Test
    void removeBook_success() {
        // Given
        Book book = new Book("1", "Test", "Ufuk", 2020, 10);
        when(libraryCrud.findById("1")).thenReturn(Optional.of(book));

        // When
        libraryService.removeBook(book.getISBN());

        // Then
        verify(libraryCache, times(1)).removeCache(book.getISBN());
        verify(libraryCrud, times(1)).deleteById(book.getISBN());
    }

    @Test
    void removeBook_no_book_at_catalogue() {
        // Given
        Book book = new Book("1", "Test", "Ufuk", 2020, 10);
        when(libraryCrud.findById("1")).thenReturn(Optional.empty()); // Simulate existing book

        //Then
        ResponseStatusException responseStatusException = assertThrows(ResponseStatusException.class, () -> libraryService.removeBook(book.getISBN()));

        Assertions.assertTrue(responseStatusException.getStatusCode().is4xxClientError());
        Assertions.assertEquals(responseStatusException.getStatusCode().value(), 404);

        verify(libraryCache, never()).removeCache(book.getISBN());
        verify(libraryCrud, never()).save(any(Book.class));
    }

    @Test
    void borrow_book_success() {
        // Given
        Book book = new Book("1", "Test", "Ufuk", 2020, 10);
        when(libraryCrud.findById("1")).thenReturn(Optional.of(book));
        when(libraryCrud.save(any(Book.class))).thenReturn(book);

        // When
        Book borrowedBook = libraryService.borrowBook("1");

        // Then
        assertEquals(9, borrowedBook.getAvailablecopies());
        verify(libraryCache, times(1)).addCache(book.getISBN(), book);
        verify(libraryCrud, times(1)).save(book);
    }

    @Test
    void borrow_book_no_book_in_catalogue() {
        // Given
        Book book = new Book("1", "Test", "Ufuk", 2020, 10);
        when(libraryCrud.findById("1")).thenReturn(Optional.empty());
        when(libraryCrud.save(any(Book.class))).thenReturn(book);

        // When

        ResponseStatusException responseStatusException = assertThrows(ResponseStatusException.class, () -> libraryService.borrowBook(book.getISBN()));
        Assertions.assertTrue(responseStatusException.getStatusCode().is4xxClientError());
        // Then
        verify(libraryCache, never()).addCache(book.getISBN(), book);
        Assertions.assertEquals(responseStatusException.getStatusCode().value(), 404);

        verify(libraryCrud, never()).save(book);
    }

    @Test
    void borrow_book_not_enough_quantity() {
        // Given
        Book book = new Book("1", "Test", "Ufuk", 2020, 0);
        when(libraryCrud.findById("1")).thenReturn(Optional.of(book));
        when(libraryCrud.save(any(Book.class))).thenReturn(book);


        // Then
        ResponseStatusException responseStatusException = assertThrows(ResponseStatusException.class, () -> libraryService.borrowBook(book.getISBN()));

        Assertions.assertTrue(responseStatusException.getStatusCode().is4xxClientError());
        Assertions.assertEquals(responseStatusException.getStatusCode().value(), 409);
        verify(libraryCache, never()).addCache(book.getISBN(), book);
        verify(libraryCrud, times(0)).save(book);
    }

    @Test
    void find_book_by_isbn_empty() {
        // Given
        Book book = new Book("1", "Test", "Ufuk", 2020, 10);
        when(libraryCrud.findById("1")).thenReturn(Optional.empty());


        // Then
        ResponseStatusException responseStatusException = assertThrows(ResponseStatusException.class, () -> libraryService.findBookByISBN(book.getISBN()));

        Assertions.assertTrue(responseStatusException.getStatusCode().is4xxClientError());
        Assertions.assertEquals(responseStatusException.getStatusCode().value(), 404);
        verify(libraryCache, times(1)).getCache(book.getISBN());
        verify(libraryCrud, times(1)).findById(book.getISBN());
    }

    @Test
    void find_book_by_isbn() {
        // Given
        Book book = new Book("1", "Test", "Ufuk", 2020, 10);
        when(libraryCrud.findById("1")).thenReturn(Optional.of(book));


        // Then
        Book bookByISBN = libraryService.findBookByISBN(book.getISBN());

        verify(libraryCache, times(1)).getCache(book.getISBN());
        verify(libraryCrud, times(1)).findById(book.getISBN());
    }


    @Test
    void find_book_by_author_empty() {
        // Given
        Book book = new Book("1", "Test", "Ufuk", 2020, 10);
        when(libraryCrud.findByAuthor("ufuk")).thenReturn(new ArrayList<>());


        // Then
        List<Book> bookByISBN = libraryService.findByAuthor(book.getAuthor());
        Assertions.assertTrue(bookByISBN.isEmpty());
        verify(libraryCrud, times(1)).findByAuthor(book.getAuthor());
    }

    @Test
    void find_book_by_author() {
        // Given
        Book book = new Book("1", "Test", "Ufuk", 2020, 10);
        when(libraryCrud.findByAuthor("Ufuk")).thenReturn(List.of(book));


        // Then
        List<Book> bookByISBN = libraryService.findByAuthor(book.getAuthor());
        assertEquals(1, bookByISBN.size());
        verify(libraryCrud, times(1)).findByAuthor(book.getAuthor());
    }
}
