package com.brus5.lukaszkrawczak.fitx.converter;

public class StringConverter
{
    public static String toUpperFirstLetter(String name)
    {
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }
}