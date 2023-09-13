package com.rajesh.admin.brand;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/*
* @Author : Anuj Kumar Rajesh
*/

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Brand Not Found")
public class BrandNotFoundRestException extends Exception {

}
