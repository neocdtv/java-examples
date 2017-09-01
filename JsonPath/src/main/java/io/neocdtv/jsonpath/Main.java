/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.neocdtv.jsonpath;

import static com.jayway.jsonpath.Criteria.where;
import com.jayway.jsonpath.Filter;
import static com.jayway.jsonpath.Filter.filter;
import com.jayway.jsonpath.JsonPath;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author xix
 */
public class Main {
    public static void main(String[] args) throws FileNotFoundException, IOException {
        final String json = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream("simple.json"));
        Filter cheapFictionFilter = filter(where("category").is("reference"));
        Object objectOne = JsonPath.read(json, "$..book[?].category", cheapFictionFilter);
        System.out.println(objectOne.getClass());
        System.out.println(objectOne);
        Object objectTwo = JsonPath.read(json, "$..book[?(@.category=='reference')].category");
        System.out.println(objectTwo);
        System.out.println(objectTwo.getClass());
    }
}
