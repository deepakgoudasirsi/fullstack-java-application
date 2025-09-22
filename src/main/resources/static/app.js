/**
 * Full-Stack Java Application - Frontend JavaScript
 * Demonstrates modern JavaScript practices with API integration
 */

// Global variables
let currentUser = null;
let users = [];
let tasks = [];
let currentSection = 'dashboard';

// API Configuration
const API_BASE_URL = 'http://localhost:8080/api';

// Utility Functions
const showToast = (message, type = 'info') => {
    const toast = document.getElementById('toast');
    const toastMessage = document.getElementById('toastMessage');
    const toastHeader = toast.querySelector('.toast-header i');
    
    toastMessage.textContent = message;
    
    // Update icon based on type
    toastHeader.className = `fas me-2 ${
        type === 'success' ? 'fa-check-circle text-success' :
        type === 'error' ? 'fa-exclamation-circle text-danger' :
        type === 'warning' ? 'fa-exclamation-triangle text-warning' :
        'fa-info-circle text-primary'
    }`;
    
    const bsToast = new bootstrap.Toast(toast);
    bsToast.show();
};

const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString() + ' ' + date.toLocaleTimeString();
};

const formatDateTime = (dateString) => {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleString();
};

const getStatusBadge = (status) => {
    const statusMap = {
        'PENDING': 'bg-warning',
        'IN_PROGRESS': 'bg-info',
        'COMPLETED': 'bg-success',
        'CANCELLED': 'bg-secondary'
    };
    return `<span class="badge ${statusMap[status] || 'bg-secondary'}">${status.replace('_', ' ')}</span>`;
};

const getPriorityBadge = (priority) => {
    const priorityMap = {
        'LOW': 'bg-success',
        'MEDIUM': 'bg-warning',
        'HIGH': 'bg-danger',
        'URGENT': 'bg-danger'
    };
    return `<span class="badge ${priorityMap[priority] || 'bg-secondary'}">${priority}</span>`;
};

// API Functions
const apiCall = async (url, options = {}) => {
    try {
        const response = await fetch(`${API_BASE_URL}${url}`, {
            headers: {
                'Content-Type': 'application/json',
                ...options.headers
            },
            ...options
        });
        
        if (!response.ok) {
            const errorData = await response.json().catch(() => ({ message: 'An error occurred' }));
            throw new Error(errorData.message || `HTTP ${response.status}`);
        }
        
        return await response.json();
    } catch (error) {
        console.error('API Error:', error);
        throw error;
    }
};

// User Management Functions
const loadUsers = async () => {
    try {
        users = await apiCall('/users');
        populateUserSelects();
        renderUsersTable();
    } catch (error) {
        showToast(`Failed to load users: ${error.message}`, 'error');
    }
};

const createUser = async () => {
    try {
        const userData = {
            username: document.getElementById('username').value,
            email: document.getElementById('email').value,
            password: document.getElementById('password').value,
            firstName: document.getElementById('firstName').value,
            lastName: document.getElementById('lastName').value
        };
        
        const newUser = await apiCall('/users', {
            method: 'POST',
            body: JSON.stringify(userData)
        });
        
        showToast('User created successfully!', 'success');
        bootstrap.Modal.getInstance(document.getElementById('createUserModal')).hide();
        document.getElementById('createUserForm').reset();
        loadUsers();
    } catch (error) {
        showToast(`Failed to create user: ${error.message}`, 'error');
    }
};

const deactivateUser = async (userId) => {
    if (!confirm('Are you sure you want to deactivate this user?')) return;
    
    try {
        await apiCall(`/users/${userId}`, { method: 'DELETE' });
        showToast('User deactivated successfully!', 'success');
        loadUsers();
    } catch (error) {
        showToast(`Failed to deactivate user: ${error.message}`, 'error');
    }
};

const activateUser = async (userId) => {
    try {
        await apiCall(`/users/${userId}/activate`, { method: 'PUT' });
        showToast('User activated successfully!', 'success');
        loadUsers();
    } catch (error) {
        showToast(`Failed to activate user: ${error.message}`, 'error');
    }
};

const populateUserSelects = () => {
    const userSelects = ['userFilter', 'taskUser'];
    userSelects.forEach(selectId => {
        const select = document.getElementById(selectId);
        if (select) {
            const currentValue = select.value;
            select.innerHTML = selectId === 'userFilter' ? '<option value="">All Users</option>' : '<option value="">Select User</option>';
            
            users.forEach(user => {
                if (user.isActive) {
                    const option = document.createElement('option');
                    option.value = user.id;
                    option.textContent = `${user.username} (${user.email})`;
                    select.appendChild(option);
                }
            });
            
            select.value = currentValue;
        }
    });
};

const renderUsersTable = () => {
    const tbody = document.getElementById('usersTableBody');
    if (!tbody) return;
    
    if (users.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted">No users found</td></tr>';
        return;
    }
    
    tbody.innerHTML = users.map(user => `
        <tr>
            <td>${user.username}</td>
            <td>${user.email}</td>
            <td>${user.firstName || ''} ${user.lastName || ''}</td>
            <td><span class="badge bg-info">${user.role}</span></td>
            <td>${user.isActive ? '<span class="badge bg-success">Active</span>' : '<span class="badge bg-secondary">Inactive</span>'}</td>
            <td>${formatDate(user.createdAt)}</td>
            <td>
                <button class="btn btn-sm ${user.isActive ? 'btn-warning' : 'btn-success'}" 
                        onclick="${user.isActive ? 'deactivateUser' : 'activateUser'}(${user.id})">
                    <i class="fas fa-${user.isActive ? 'ban' : 'check'}"></i>
                </button>
            </td>
        </tr>
    `).join('');
};

// Task Management Functions
const loadTasks = async () => {
    try {
        // Load all tasks by getting tasks for each user
        const allTasks = [];
        for (const user of users) {
            try {
                const userTasks = await apiCall(`/tasks/user/${user.id}`);
                allTasks.push(...userTasks);
            } catch (error) {
                console.warn(`Failed to load tasks for user ${user.id}:`, error);
            }
        }
        tasks = allTasks;
        renderTasksTable();
        updateDashboardStats();
    } catch (error) {
        showToast(`Failed to load tasks: ${error.message}`, 'error');
    }
};

const createTask = async () => {
    try {
        const taskData = {
            userId: parseInt(document.getElementById('taskUser').value),
            title: document.getElementById('taskTitle').value,
            description: document.getElementById('taskDescription').value,
            priority: document.getElementById('taskPriority').value,
            dueDate: document.getElementById('taskDueDate').value ? new Date(document.getElementById('taskDueDate').value).toISOString() : null
        };
        
        const newTask = await apiCall('/tasks', {
            method: 'POST',
            body: JSON.stringify(taskData)
        });
        
        showToast('Task created successfully!', 'success');
        bootstrap.Modal.getInstance(document.getElementById('createTaskModal')).hide();
        document.getElementById('createTaskForm').reset();
        loadTasks();
    } catch (error) {
        showToast(`Failed to create task: ${error.message}`, 'error');
    }
};

const updateTaskStatus = async (taskId, newStatus) => {
    try {
        await apiCall(`/tasks/${taskId}/status`, {
            method: 'PUT',
            body: JSON.stringify({ status: newStatus })
        });
        
        showToast('Task status updated successfully!', 'success');
        loadTasks();
    } catch (error) {
        showToast(`Failed to update task status: ${error.message}`, 'error');
    }
};

const deleteTask = async (taskId) => {
    if (!confirm('Are you sure you want to delete this task?')) return;
    
    try {
        await apiCall(`/tasks/${taskId}`, { method: 'DELETE' });
        showToast('Task deleted successfully!', 'success');
        loadTasks();
    } catch (error) {
        showToast(`Failed to delete task: ${error.message}`, 'error');
    }
};

const filterTasks = () => {
    const userFilter = document.getElementById('userFilter').value;
    const statusFilter = document.getElementById('statusFilter').value;
    const priorityFilter = document.getElementById('priorityFilter').value;
    
    let filteredTasks = tasks;
    
    if (userFilter) {
        filteredTasks = filteredTasks.filter(task => task.user.id == userFilter);
    }
    
    if (statusFilter) {
        filteredTasks = filteredTasks.filter(task => task.status === statusFilter);
    }
    
    if (priorityFilter) {
        filteredTasks = filteredTasks.filter(task => task.priority === priorityFilter);
    }
    
    renderTasksTable(filteredTasks);
};

const renderTasksTable = (tasksToRender = tasks) => {
    const tbody = document.getElementById('tasksTableBody');
    if (!tbody) return;
    
    if (tasksToRender.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted">No tasks found</td></tr>';
        return;
    }
    
    tbody.innerHTML = tasksToRender.map(task => `
        <tr>
            <td class="text-truncate" title="${task.title}">${task.title}</td>
            <td class="text-truncate" title="${task.description || 'No description'}">${task.description || 'No description'}</td>
            <td>${getStatusBadge(task.status)}</td>
            <td>${getPriorityBadge(task.priority)}</td>
            <td>${formatDate(task.dueDate)}</td>
            <td>${task.user.username}</td>
            <td>
                <div class="btn-group btn-group-sm">
                    <select class="form-select form-select-sm" onchange="updateTaskStatus(${task.id}, this.value)" style="width: auto;">
                        <option value="PENDING" ${task.status === 'PENDING' ? 'selected' : ''}>Pending</option>
                        <option value="IN_PROGRESS" ${task.status === 'IN_PROGRESS' ? 'selected' : ''}>In Progress</option>
                        <option value="COMPLETED" ${task.status === 'COMPLETED' ? 'selected' : ''}>Completed</option>
                        <option value="CANCELLED" ${task.status === 'CANCELLED' ? 'selected' : ''}>Cancelled</option>
                    </select>
                    <button class="btn btn-outline-danger btn-sm" onclick="deleteTask(${task.id})">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </td>
        </tr>
    `).join('');
};

// Dashboard Functions
const updateDashboardStats = () => {
    const totalTasks = tasks.length;
    const pendingTasks = tasks.filter(task => task.status === 'PENDING').length;
    const inProgressTasks = tasks.filter(task => task.status === 'IN_PROGRESS').length;
    const completedTasks = tasks.filter(task => task.status === 'COMPLETED').length;
    
    document.getElementById('totalTasks').textContent = totalTasks;
    document.getElementById('pendingTasks').textContent = pendingTasks;
    document.getElementById('inProgressTasks').textContent = inProgressTasks;
    document.getElementById('completedTasks').textContent = completedTasks;
    
    // Update recent activity
    const recentActivity = document.getElementById('recentActivity');
    if (tasks.length > 0) {
        const recentTasks = tasks
            .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
            .slice(0, 5);
        
        recentActivity.innerHTML = recentTasks.map(task => `
            <div class="d-flex justify-content-between align-items-center mb-2">
                <span class="text-truncate">${task.title}</span>
                <small class="text-muted">${formatDate(task.createdAt)}</small>
            </div>
        `).join('');
    } else {
        recentActivity.innerHTML = '<p class="text-muted">No recent activity</p>';
    }
};

// Navigation Functions
const showSection = (sectionId) => {
    // Hide all sections
    document.querySelectorAll('.content-section').forEach(section => {
        section.style.display = 'none';
    });
    
    // Show selected section
    document.getElementById(sectionId).style.display = 'block';
    
    // Update navigation
    document.querySelectorAll('.nav-link').forEach(link => {
        link.classList.remove('active');
    });
    
    // Update current section
    currentSection = sectionId.replace('-section', '');
};

const showDashboard = () => {
    showSection('dashboard-section');
    updateDashboardStats();
};

const showTasks = () => {
    showSection('tasks-section');
    loadTasks();
};

const showUsers = () => {
    showSection('users-section');
    loadUsers();
};

// Modal Functions
const showCreateUserModal = () => {
    const modal = new bootstrap.Modal(document.getElementById('createUserModal'));
    modal.show();
};

const showCreateTaskModal = () => {
    const modal = new bootstrap.Modal(document.getElementById('createTaskModal'));
    modal.show();
};

// Initialize Application
const initializeApp = async () => {
    try {
        showToast('Application initialized successfully!', 'success');
        await loadUsers();
        await loadTasks();
        showDashboard();
    } catch (error) {
        showToast(`Failed to initialize application: ${error.message}`, 'error');
    }
};

// Event Listeners
document.addEventListener('DOMContentLoaded', () => {
    initializeApp();
    
    // Add event listeners for form submissions
    document.getElementById('createUserForm').addEventListener('submit', (e) => {
        e.preventDefault();
        createUser();
    });
    
    document.getElementById('createTaskForm').addEventListener('submit', (e) => {
        e.preventDefault();
        createTask();
    });
});

// Export functions for global access
window.showDashboard = showDashboard;
window.showTasks = showTasks;
window.showUsers = showUsers;
window.showCreateUserModal = showCreateUserModal;
window.showCreateTaskModal = showCreateTaskModal;
window.createUser = createUser;
window.createTask = createTask;
window.deactivateUser = deactivateUser;
window.activateUser = activateUser;
window.updateTaskStatus = updateTaskStatus;
window.deleteTask = deleteTask;
window.loadTasks = loadTasks;
window.filterTasks = filterTasks;
