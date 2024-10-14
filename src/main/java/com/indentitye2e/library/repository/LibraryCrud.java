package com.indentitye2e.library.repository;

import com.indentitye2e.library.model.Book;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface LibraryCrud extends MongoRepository<Book, String> {

    List<Book> findByAuthor(String isbn);
}
