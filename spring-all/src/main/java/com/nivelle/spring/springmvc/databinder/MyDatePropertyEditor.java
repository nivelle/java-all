package com.nivelle.spring.springmvc.databinder;

import java.beans.PropertyEditorSupport;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 自定义日期
 *
 * @author fuxinzhong
 * @date 2021/04/18
 */
public class MyDatePropertyEditor extends PropertyEditorSupport {
    private static final String PATTERN = "yyyy-MM-dd";

    @Override
    public String getAsText() {
        Date date = (Date) super.getValue();
        return new SimpleDateFormat(PATTERN).format(date);
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        try {
            super.setValue(new SimpleDateFormat(PATTERN).parse(text));
        } catch (ParseException e) {
            System.out.println("ParseException....................");
        }
    }
}

