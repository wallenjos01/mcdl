package org.wallentines.mcdl;

public interface Task {

    Result run(TaskQueue queue);


    class Result {

        private final String error;

        private Result(String error) {
            this.error = error;
        }

        public static Result success() {
            return new Result(null);
        }

        public static Result error(String error) {
            return new Result(error);
        }

        public boolean isError() {
            return error != null;
        }

        public String getErrorMessage() {
            return error;
        }
    }

}
