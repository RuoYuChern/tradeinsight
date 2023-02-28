package com.tao.trade.api;

import com.tao.trade.api.facade.TaoResponse;
import com.tao.trade.infra.TuShareClient;
import com.tao.trade.proccess.JobActors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminApi {
    @Autowired
    private TuShareClient tuShareClient;

    @Autowired
    private JobActors jobActors;

    @PostMapping("/history-load")
    public TaoResponse<Void> history(){
        TaoResponse<Void> rsp = new TaoResponse<>();
        jobActors.addHistory();
        rsp.setStatus(200);
        rsp.setMsg("OK");
        return rsp;
    }
}
