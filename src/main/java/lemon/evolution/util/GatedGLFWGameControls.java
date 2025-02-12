package lemon.evolution.util;

import lemon.engine.event.EventWith;
import lemon.engine.event.Observable;
import lemon.engine.glfw.GLFWInput;
import lemon.engine.toolbox.Disposable;
import lemon.engine.toolbox.Disposables;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class GatedGLFWGameControls<T> implements GameControls<T, GLFWInput>, Disposable {
	private final GLFWGameControls<T> baseControls;
	private final Map<T, Observable<Boolean>> controls = new HashMap<>();
	private final Observable<Boolean> enabled = new Observable<>(true);
	private final Disposables disposables = new Disposables();

	public GatedGLFWGameControls(GLFWGameControls<T> baseControls) {
		this.baseControls = baseControls;
	}

	@Override
	public <U> void addCallback(Function<GLFWInput, EventWith<U>> inputEvent, Consumer<? super U> callback) {
		baseControls.addCallback(inputEvent, event -> {
			if (enabled.getValue()) {
				callback.accept(event);
			}
		});
	}

	@Override
	public Observable<Boolean> activated(T control) {
		return controls.computeIfAbsent(control, c -> Observable.ofAnd(baseControls.activated(control), enabled, disposables::add));
	}

	@Override
	public void dispose() {
		disposables.dispose();
	}

	public void setEnabled(boolean enabled) {
		this.enabled.setValue(enabled);
	}
}
