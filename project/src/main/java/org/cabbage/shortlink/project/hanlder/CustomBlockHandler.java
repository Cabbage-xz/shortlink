package org.cabbage.shortlink.project.hanlder;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import org.cabbage.shortlink.common.convention.result.Result;
import org.cabbage.shortlink.common.dto.resp.ShortLinkCreateRespDTO;

/**
 * @author xzcabbage
 * @since 2025/10/7
 */
public class CustomBlockHandler {

    public static Result<ShortLinkCreateRespDTO> createShortLinkBlockHandlerMethod(ShortLinkCreateRespDTO req, BlockException exception) {
        return new Result<ShortLinkCreateRespDTO>().setCode("B10000").setMessage("当前访问网站人数过多，请稍后再试");
    }
}
