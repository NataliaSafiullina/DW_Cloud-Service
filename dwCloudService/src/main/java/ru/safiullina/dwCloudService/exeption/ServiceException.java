package ru.safiullina.dwCloudService.exeption;

import org.springframework.http.HttpStatus;

public class ServiceException extends RuntimeException {

    public ServiceException () {
        super();
    }

    public ServiceException(HttpStatus httpStatus, String message){
        super(httpStatus + message);
    }

}
