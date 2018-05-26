package com.borderx.packamule.auth;

import javax.servlet.http.HttpServletRequest;

public interface IAuth {

    boolean check(HttpServletRequest httpReq, Capability capability);
}
