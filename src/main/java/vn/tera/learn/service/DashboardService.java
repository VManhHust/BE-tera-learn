package vn.tera.learn.service;

import vn.tera.learn.dto.DashboardResponse;

public interface DashboardService {
    DashboardResponse getDashboard(Long userId);
}
