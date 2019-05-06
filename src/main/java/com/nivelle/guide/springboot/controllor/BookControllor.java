package com.nivelle.guide.springboot.controllor;

import com.google.common.collect.Lists;
import com.nivelle.guide.springboot.pojo.Book;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller("/books")
public class BookControllor {


    @PostMapping(value = "post/books",
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
        List<Book> resultBook = books.stream().filter(x -> x.getId().equals(book.getId())).collect(Collectors.toList());
        return resultBook.get(0);

    }

    @RequestMapping(value = "/book")
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
