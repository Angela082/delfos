package delfos.experiment.casestudy;

import java.io.PrintStream;
import java.util.Date;
import delfos.common.Chronometer;
import delfos.common.DateCollapse;

/**
 * Listener por defecto que imprime los eventos de cambio en un Stream de
 * salida. Permite limitar la salida para que se haga una vez cada X
 * milisegundos, como muy rápido, en caso de que el estado sea el mismo.
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 14-Mayo-2013
 */
public class ExecutionProgressListener_onlyChanges implements ExecutionProgressListener {

    /**
     * Cronómetro para controlar el tiempo entre escrituras.
     */
    private final Chronometer chronometer;
    /**
     * Stream de salida para escribir los mensajes.
     */
    private final PrintStream out;
    /**
     * Último porcentaje registrado e imprimido en el stream.
     */
    private int lastProgressPercent = -1;
    /**
     * Última tarea registrada e imprimido en el stream.
     */
    private String lastProgressJob = "emptyJob";
    /**
     * Tiempo mínimo que transcurre entre escrituras.
     */
    private final long verbosePeriod;
    private boolean beginPrinted = false;
    private boolean endPrinted;

    /**
     * Constructor por defecto, que establece el stream donde se escribe la
     * información de progreso y se limita el número de escrituras por tiempo.
     *
     * @param out Stream de salida en el que se escriben los mensajes.
     * @param verbosePeriod Tiempo mínimo entre escrituras.
     */
    public ExecutionProgressListener_onlyChanges(PrintStream out, long verbosePeriod) {
        this.out = out;
        this.verbosePeriod = verbosePeriod;
        chronometer = new Chronometer();
    }

    @Override
    public void executionProgressChanged(String proceso, int percent, long remainingMiliSeconds) {

        if (percent == 0) {
            printInfo(proceso, percent, remainingMiliSeconds);
            beginPrinted = true;
        } else {
            beginPrinted = false;
            if (percent == 100) {
                printInfo(proceso, percent, remainingMiliSeconds);
                endPrinted = true;
            } else {
                endPrinted = false;
            }
        }

        boolean repeated = percent == lastProgressPercent && proceso.equals(lastProgressJob);
        boolean timeTrigger = chronometer.getTotalElapsed() >= verbosePeriod;
        if (!repeated || timeTrigger) {

            printInfo(proceso, percent, remainingMiliSeconds);

        }
    }

    private void printInfo(String actualJob, int percent, long remainingTime) {
        String message = new Date().toString() + " -- " + actualJob + " --> "
                + percent + "% --> "
                + DateCollapse.collapse(remainingTime);
        out.println(message);
        chronometer.reset();
        lastProgressJob = actualJob;
        lastProgressPercent = percent;

    }
}
