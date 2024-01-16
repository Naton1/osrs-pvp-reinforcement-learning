package com.github.naton1.rl.util;

import com.github.oxo42.stateless4j.delegates.Trace;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class LoggingStateMachineTrace<S, T> implements Trace<S, T> {

    private final String id;

    @Override
    public void trigger(final T trigger) {
        log.debug("Running {} for {}", trigger, id);
    }

    @Override
    public void transition(final T trigger, final S source, final S destination) {
        log.debug("Running {}: {} -> {} for {}", trigger, source, destination, id);
    }
}
