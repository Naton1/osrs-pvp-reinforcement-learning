package com.github.naton1.rl.env;

import com.github.naton1.rl.env.dharok.DharokEnvironmentDescriptor;
import com.github.naton1.rl.env.nh.NhEnvironmentDescriptor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum EnvironmentRegistry {
    NH("NhEnv", new NhEnvironmentDescriptor()),
    DHAROK("DharokEnv", new DharokEnvironmentDescriptor());

    private final String type;
    private final EnvironmentDescriptor<?> environmentDescriptor;
}
