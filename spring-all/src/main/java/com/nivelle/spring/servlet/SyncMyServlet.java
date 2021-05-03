package com.nivelle.spring.servlet;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2021/05/03
 */
@WebServlet(urlPatterns = "/mySyncMyServlet")
public class SyncMyServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("begin servlet.....");

        try {
            Thread.sleep(3000);
            response.setContentType("text/html");
            PrintWriter out = response.getWriter();
            out.println("<html>");
            out.println("<head>");
            out.println("<title>hello world</title>");
            out.println("<body>");
            out.println("<h1>this is my page</h1>");
            out.println("</body>");
            out.println("</body>");
            out.println("</html>");
        } catch (Exception e) {

        }
        System.out.println("end servlet.....");
    }

}
