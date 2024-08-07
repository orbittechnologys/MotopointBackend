package com.ot.moto.dto;

import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Data
public class ResponseStructure<T> {
    private int status;

    private String message;

    private T data;

    public static HttpStatus getStatus(int status){
        return switch (status) {
            case 201 -> HttpStatus.CREATED;
            case 200 -> HttpStatus.OK;
            case 202 -> HttpStatus.ACCEPTED;
            case 400 -> HttpStatus.BAD_REQUEST;
            case 401 -> HttpStatus.UNAUTHORIZED;
            case 403 -> HttpStatus.FORBIDDEN;
            case 404 -> HttpStatus.NOT_FOUND;
            case 409 -> HttpStatus.CONFLICT;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    public static ResponseEntity<ResponseStructure<Object>> successResponse(Object data, String message){
        ResponseStructure<Object> respStructure = new ResponseStructure<>();
        respStructure.setData(data);
        respStructure.setStatus(HttpStatus.OK.value());
        respStructure.setMessage(message);
        return new ResponseEntity<>(respStructure,HttpStatus.OK);
    }

    public static ResponseEntity<ResponseStructure<Object>> errorResponse(Object data,int status, String message){
        ResponseStructure<Object> respStructure = new ResponseStructure<>();
        respStructure.setData(data);
        respStructure.setStatus(status);
        respStructure.setMessage(message);
        return new ResponseEntity<>(respStructure,getStatus(status));
    }
}
