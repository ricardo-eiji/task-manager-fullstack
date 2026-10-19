package com.taskmanager;

public class InvalidCredentialsException extends RuntimeException
{
    public InvalidCredentialsException()
    {
        super("Invalid credentials");
    }
}