package com.nivelle.programming.springboot.controllor;

import com.google.common.collect.Lists;
import com.nivelle.programming.springboot.pojo.Book;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class BookControllor {


    @PostMapping(value = "books",
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
        List<Book> resultBook =  books.stream().filter(x->x.getId().equals(book.getId())).collect(Collectors.toList());
        return resultBook.get(0);

    }
}
