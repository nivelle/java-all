package com.nivelle.spring.controllor;

import com.google.common.collect.Lists;
import com.nivelle.spring.pojo.Book;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping(value = "test/books")
public class BookController {


    @RequestMapping(value = "/book", consumes = MediaType.APPLICATION_XML_VALUE,
            produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public Book getAllBooks(@RequestBody Book book) {

        List<Book> books = Lists.newArrayList();

        Book book1 = new Book(1L, "java");
        Book book2 = new Book(2L, "C++");
        Book book3 = new Book(3L, "Golang");
        books.add(book1);
        books.add(book2);
        books.add(book3);
        books.add(book);
//        List<Book> resultBook = books.stream().filter(x -> x.getId().equals(book.getId())).collect(Collectors.toList());
        return books.get(0);

    }

    @GetMapping(value = "/books")
    @ResponseBody
    public Book getBooks() {

        List<Book> books = Lists.newArrayList();

        Book book1 = new Book(1L, "java");
        Book book2 = new Book(2L, "C++");
        Book book3 = new Book(3L, "Golang");

        books.add(book1);
        books.add(book2);
        books.add(book3);
        //List<Book> resultBook =  books.stream().filter(x->x.getId().equals(book.getId())).collect(Collectors.toList());
        return books.get(0);

    }
}
