package com.taskmanager;

public class UsernameAlreadyExistsException extends RuntimeException
{
    public UsernameAlreadyExistsException()
    {
        super("Username already taken");
    }
}