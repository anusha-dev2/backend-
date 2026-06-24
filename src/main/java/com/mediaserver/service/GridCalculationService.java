package com.mediaserver.service;

import org.springframework.stereotype.Service;

@Service
public class GridCalculationService {

    public static class GridResult {
        private int rows;
        private int columns;

        public GridResult(int rows, int columns) {
            this.rows = rows;
            this.columns = columns;
        }

        public int getRows() {
            return rows;
        }

        public int getColumns() {
            return columns;
        }
    }

    public GridResult calculateGrid(int deviceCount) {
        if (deviceCount <= 0) {
            return new GridResult(1, 1);
        }
        
        int cols = (int) Math.ceil(Math.sqrt(deviceCount));
        int rows = (int) Math.ceil((double) deviceCount / cols);
        
        return new GridResult(rows, cols);
    }
}
