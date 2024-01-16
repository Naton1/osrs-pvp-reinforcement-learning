package com.github.naton1.rl.env.dharok;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DharokEnvironmentParams {

    private String episodeId = "";

    private String agent = "";
    private String target = "";
}
